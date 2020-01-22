/*
 * Copyright (c) 2019 MNS Software Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mnssoftware.validator.openapi.service.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.mnssoftware.validator.openapi.service.ValidationException;
import com.mnssoftware.validator.openapi.service.openapi.parameter.ParameterType;
import com.mnssoftware.validator.openapi.utils.JsonUtils;
import com.networknt.jsonoverlay.Overlay;
import com.networknt.oas.model.Parameter;
import com.networknt.oas.model.RequestBody;
import com.networknt.oas.model.impl.SchemaImpl;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import static com.mnssoftware.validator.openapi.service.ValidationKeyMessage.MISSING_BODY;
import static com.mnssoftware.validator.openapi.service.ValidationKeyMessage.UNEXPECTED_BODY;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

/**
 * Validate a request data against a given API operation defined in the OpenAPI Spec.
 * The specific operation is looked up by API endpoint (path + httpMethod)
 *
 * @author Mark Silcox
 */
public class RequestValidator {

    static final Logger logger = LoggerFactory.getLogger(RequestValidator.class);
    static final String VALIDATOR_REQUEST_BODY_UNEXPECTED = "ERR11013";
    static final String VALIDATOR_REQUEST_BODY_MISSING = "ERR11014";
    static final String VALIDATOR_REQUEST_PARAMETER_HEADER_MISSING = "ERR11017";
    static final String VALIDATOR_REQUEST_PARAMETER_QUERY_MISSING = "ERR11000";

    private final SchemaValidator schemaValidator;

    /**
     * Construct a new request validator with the given schema validator.
     *
     * @param schemaValidator The schema validator to use when validating request bodies
     */
    public RequestValidator(final SchemaValidator schemaValidator) {
        this.schemaValidator = requireNonNull(schemaValidator, "A schema validator is required");
    }

    /**
     * Validate the request against the given API operation
     *
     * @param requestPath      normalised path
     * @param request          The HttpServletRequest to validate
     * @param openApiOperation OpenAPI operation
     * @return A validation report containing validation errors
     */
    public Pair<String, Set<ValidationMessage>> validateRequest(final NormalisedPath requestPath, HttpServletRequest request, OpenApiOperation openApiOperation) {
        requireNonNull(requestPath, "A request path is required");
        requireNonNull(request, "An request is required");
        requireNonNull(openApiOperation, "An OpenAPI operation is required");

        Set<ValidationMessage> processingReport = validateRequestParameters(request, requestPath, openApiOperation);
        if (!CollectionUtils.isEmpty(processingReport))
            return Pair.of("request parameter", processingReport);

        String contentType = request.getHeader("CONTENT_TYPE");
        if (contentType == null || contentType.startsWith("application/json")) {

            processingReport = validateRequestBody(request, openApiOperation);
            if (!CollectionUtils.isEmpty(processingReport))
                return Pair.of("field", processingReport);
        }
        return Pair.of(null, Collections.emptySet());
    }

    private Set<ValidationMessage> validateRequestBody(HttpServletRequest request, final OpenApiOperation openApiOperation) {
        final RequestBody specBody = openApiOperation.getOperation().getRequestBody();

        try {
            JsonNode requestBody = JsonUtils.readTree(request.getReader());
            if (requestBody != null && specBody == null) {
                return singleton(OpenApiHelper.buildValidationMessage(UNEXPECTED_BODY.getCode(), "body"));
            }

            if (specBody == null) {
                return Collections.emptySet();
            }

            if (requestBody instanceof MissingNode) {
                if (specBody.getRequired()) {
                    return singleton(OpenApiHelper.buildValidationMessage(MISSING_BODY.getCode(), "body"));
                }
                return Collections.emptySet();
            }

            SchemaValidatorsConfig config = new SchemaValidatorsConfig();
            config.setTypeLoose(false);

            return schemaValidator.validate(requestBody, Overlay.toJson((SchemaImpl) specBody.getContentMediaType("application/json").getSchema()), config);
        } catch (IOException ex) {
            throw new ValidationException(Collections.singletonList("The payload could not be parsed"));
        }
    }

    private Set<ValidationMessage> validateRequestParameters(final HttpServletRequest request, final NormalisedPath requestPath, final OpenApiOperation openApiOperation) {
        Set<ValidationMessage> status = validatePathParameters(requestPath, openApiOperation);
        if (status != null) return status;

        status = validateQueryParameters(request, openApiOperation);
        if (status != null) return status;

        return null;
    }

    private Set<ValidationMessage> validatePathParameters(final NormalisedPath requestPath, final OpenApiOperation openApiOperation) {
        List<Parameter> pathParameters = openApiOperation.getOperation().getParameters().stream()
                .filter(p -> ParameterType.is(p.getIn(), ParameterType.PATH))
                .collect(Collectors.toList());

        for (int i = 0; i < openApiOperation.getPathString().parts().size(); i++) {
            if (!openApiOperation.getPathString().isParam(i)) {
                continue;
            }

            final String paramName = openApiOperation.getPathString().paramName(i);
            final Optional<Parameter> parameter = pathParameters
                    .stream()
                    .filter(p -> p.getName().equalsIgnoreCase(paramName))
                    .findFirst();

            if (parameter.isPresent()) {
                String paramValue = requestPath.part(i); // If it can't be UTF-8 decoded, use directly.
                try {
                    paramValue = URLDecoder.decode(requestPath.part(i), "UTF-8");
                } catch (Exception e) {
                    logger.info("Path parameter cannot be decoded, it will be used directly");
                }

                return schemaValidator.validate(paramValue, Overlay.toJson((SchemaImpl) (parameter.get().getSchema())));
            }
        }
        return Collections.emptySet();
    }

    private Set<ValidationMessage> validateQueryParameters(final HttpServletRequest request,
                                                           final OpenApiOperation openApiOperation) {
        List<Parameter> queryParameters = openApiOperation.getOperation().getParameters().stream()
                .filter(p -> ParameterType.is(p.getIn(), ParameterType.QUERY))
                .collect(Collectors.toList());

        Optional<Set<ValidationMessage>> optional = queryParameters
                .stream()
                .map(p -> validateQueryParameter(request, openApiOperation, p))
                .filter(s -> s != null)
                .findFirst();

        return optional.orElse(Collections.emptySet());
    }


    private Set<ValidationMessage> validateQueryParameter(final HttpServletRequest request,
                                                          final OpenApiOperation openApiOperation,
                                                          final Parameter queryParameter) {

        final Collection<String> queryParameterValues = Arrays.asList(ServletRequestUtils.getStringParameters(request, queryParameter.getName()));

        if ((queryParameterValues == null || queryParameterValues.isEmpty())) {
            if (queryParameter.getRequired() != null && queryParameter.getRequired()) {
                return singleton(OpenApiHelper.buildValidationMessage(VALIDATOR_REQUEST_PARAMETER_QUERY_MISSING, "query"));
            }
            // Validate the value contains by queryParameterValue, if it is the only elements inside the array deque.
            // Since if the queryParameterValue's length smaller than 2, it means the query parameter is not an array,
            // thus not necessary to apply array validation to this value.
        } else if (queryParameterValues.size() < 2) {

            Optional<Set<ValidationMessage>> optional = queryParameterValues
                    .stream()
                    .map((v) -> schemaValidator.validate(v, Overlay.toJson((SchemaImpl) queryParameter.getSchema())))
                    .filter(s -> s != null)
                    .findFirst();

            return optional.orElse(Collections.emptySet());
            // Validate the queryParameterValue directly instead of validating its elements, if the length of this array deque larger than 2.
            // Since if the queryParameterValue's length larger than 2, it means the query parameter is an array.
            // thus array validation should be applied, for example, validate the length of the array.
        } else {
            return schemaValidator.validate(queryParameterValues, Overlay.toJson((SchemaImpl) queryParameter.getSchema()));
        }
        return Collections.emptySet();
    }


    private Set<ValidationMessage> validateHeader(final HttpServletRequest request,
                                                  final OpenApiOperation openApiOperation,
                                                  final Parameter headerParameter) {
        Enumeration<String> headerValues = request.getHeaders(headerParameter.getName());
        if (headerValues == null || !headerValues.hasMoreElements()) {
            if (headerParameter.getRequired()) {
                return singleton(OpenApiHelper.buildValidationMessage(VALIDATOR_REQUEST_PARAMETER_HEADER_MISSING, "body"));
            }
        } else {
            Optional<Set<ValidationMessage>> optional = Collections.list(headerValues)
                    .stream()
                    .map((v) -> schemaValidator.validate(v, Overlay.toJson((SchemaImpl) headerParameter.getSchema())))
                    .filter(s -> s != null)
                    .findFirst();
            return optional.orElse(null);
        }
        return null;
    }

}
