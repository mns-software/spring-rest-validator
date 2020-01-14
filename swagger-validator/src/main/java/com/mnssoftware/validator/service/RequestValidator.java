package com.mnssoftware.validator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.mnssoftware.validator.service.parameter.ParameterValidators;
import com.mnssoftware.validator.service.swagger.NormalisedPath;
import com.mnssoftware.validator.service.swagger.SwaggerOperation;
import com.mnssoftware.validator.utils.JsonUtils;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.mnssoftware.validator.service.ValidationKeyMessage.*;
import static com.mnssoftware.validator.service.swagger.SwaggerHelper.buildValidationMessage;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

/**
 * Validate a request against a given API operation.
 *
 * @author msilcox
 */
public class RequestValidator {

  private final SchemaValidator schemaValidator;
  private final ParameterValidators parameterValidators;

  /**
   * Construct a new request validator with the given schema validator.
   *
   * @param schemaValidator The schema validator to use when validating request bodies
   */
  public RequestValidator(final SchemaValidator schemaValidator) {
    this.schemaValidator = requireNonNull(schemaValidator, "A schema validator is required");
    this.parameterValidators = new ParameterValidators(schemaValidator);
  }

  /**
   * Validate the request against the given API operation
   *
   * @param requestPath      swagger request path
   * @param request          servlet request
   * @param swaggerOperation swagger operation
   * @return A validation report containing validation errors
   */
  public Pair<String, Set<ValidationMessage>> validateRequest(NormalisedPath requestPath,
                                                              final HttpServletRequest request, SwaggerOperation swaggerOperation) {
    requireNonNull(request, "A request is required");

    Set<ValidationMessage> processingReport = validatePathParameters(requestPath, swaggerOperation);
    if (!CollectionUtils.isEmpty(processingReport))
      return Pair.of("path parameter", processingReport);

    processingReport = validateQueryParameters(request, swaggerOperation);
    if (!CollectionUtils.isEmpty(processingReport))
      return Pair.of("query parameter", processingReport);

    processingReport = validateRequestBody(request, swaggerOperation);
    if (!CollectionUtils.isEmpty(processingReport))
      return Pair.of("field", processingReport);

    return Pair.of(null, Collections.emptySet());
  }

  private Set<ValidationMessage> validateRequestBody(HttpServletRequest request,
                                                     final SwaggerOperation swaggerOperation) {
    final Optional<Parameter> bodyParameter = swaggerOperation.getOperation().getParameters()
            .stream().filter(p -> p.getIn().equalsIgnoreCase("body")).findFirst();
    try {

      JsonNode requestBody = JsonUtils.readTree(request.getReader());
      if (!bodyParameter.isPresent() && !(requestBody instanceof MissingNode)) {
        return singleton(buildValidationMessage(UNEXPECTED_BODY.getCode(), "body"));
      }

      if (!bodyParameter.isPresent()) {
        return Collections.emptySet();
      }

      if (requestBody instanceof MissingNode) {
        if (bodyParameter.get().getRequired()) {
          return singleton(buildValidationMessage(MISSING_BODY.getCode(), "body"));
        }
        return Collections.emptySet();
      }
      SchemaValidatorsConfig config = new SchemaValidatorsConfig();
      config.setTypeLoose(false);
      return schemaValidator.validate(requestBody, ((BodyParameter) bodyParameter.get()).getSchema());
    } catch (IOException ex) {
      throw new ValidationException(Collections.singletonList("The payload could not be parsed"));
    }
  }

  private Set<ValidationMessage> validatePathParameters(final NormalisedPath requestPath,
                                                        final SwaggerOperation swaggerOperation) {
    Set<ValidationMessage> processingReport = new HashSet<>();
    for (int i = 0; i < swaggerOperation.getPathString().parts().size(); i++) {
      if (!swaggerOperation.getPathString().isParam(i)) {
        continue;
      }

      final String paramName = swaggerOperation.getPathString().paramName(i);
      final String paramValue = requestPath.part(i);

      processingReport.addAll(swaggerOperation.getOperation().getParameters()
              .stream()
              .filter(p -> p.getIn().equalsIgnoreCase("PATH"))
              .filter(p -> p.getName().equalsIgnoreCase(paramName))
              .flatMap(p -> parameterValidators.validate(paramValue, p).stream())
              .collect(Collectors.toSet()));
    }
    return processingReport;
  }

  private Set<ValidationMessage> validateQueryParameters(final HttpServletRequest request,
                                                         final SwaggerOperation swaggerOperation) {
    return swaggerOperation
            .getOperation()
            .getParameters()
            .stream()
            .filter(p -> p.getIn().equalsIgnoreCase("QUERY"))
            .flatMap(p -> validateQueryParameter(request, p).stream()).collect(toSet());
  }

  private Set<ValidationMessage> validateQueryParameter(final HttpServletRequest request,
                                                        final Parameter queryParameter) {
    MultiValueMap<String, String> queryParams =
            UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().getQueryParams();
    final Collection<String> queryParameterValues = queryParams.get(queryParameter.getName());

    if (CollectionUtils.isEmpty(queryParameterValues)) {
      if (queryParameter.getRequired()) {
        return singleton(buildValidationMessage(NOT_NULL.getCode(), queryParameter.getName()));
      }
    } else {
      return queryParameterValues
              .stream()
              .flatMap(v -> parameterValidators.validate(v, queryParameter).stream()).collect(toSet());
    }
    return Collections.emptySet();
  }
}
