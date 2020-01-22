package com.mnssoftware.validator.swagger.service;

import com.mnssoftware.validator.swagger.service.swagger.ApiNormalisedPath;
import com.mnssoftware.validator.swagger.service.swagger.NormalisedPath;
import com.mnssoftware.validator.swagger.service.swagger.SwaggerHelper;
import com.mnssoftware.validator.swagger.service.swagger.SwaggerOperation;
import com.networknt.schema.ValidationMessage;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Slf4j
public class SwaggerValidationService implements ValidationService {

    private final Swagger swagger;

    RequestValidator requestValidator;

    public SwaggerValidationService(String swaggerDocLocation) {
        swagger = new SwaggerParser().read(swaggerDocLocation);
        final SchemaValidator schemaValidator = new SchemaValidator(swagger);
        this.requestValidator = new RequestValidator(schemaValidator);
    }

    @Override
    public void validateRequest(HttpServletRequest request) throws ServletException {
        final NormalisedPath requestPath = new ApiNormalisedPath(swagger, request.getRequestURI());
        Optional<SwaggerOperation> swaggerOperation = getSwaggerOperation(request);

        swaggerOperation.ifPresent(op -> {
            Pair<String, Set<ValidationMessage>> validationMessages = requestValidator.validateRequest(requestPath, request, op);

            List<String> errorMessages = validationMessages.getValue()
                    .stream()
                    .map(v -> Pair.of(getFieldFromJsonMessage(v.getMessage()), ValidationKeyMessage.getMessageFromCode(v.getCode())))
                    .map(p -> buildErrorMessage(validationMessages.getKey(), p.getLeft(), p.getRight()))
                    .collect(toList());

            if (!errorMessages.isEmpty()) {
                throw new ValidationException(errorMessages);
            }
        });
    }

    private Optional<SwaggerOperation> getSwaggerOperation(HttpServletRequest request) throws ServletException {
        final NormalisedPath requestPath = new ApiNormalisedPath(swagger, request.getRequestURI());
        final Optional<NormalisedPath> maybeApiPath = SwaggerHelper.findMatchingApiPath(swagger, requestPath);
        if (!maybeApiPath.isPresent()) {
            log.debug("Path '{}' is not defined in swagger documentation", requestPath.original());
            return Optional.empty();
        }

        final NormalisedPath swaggerPathString = maybeApiPath.get();
        final Path swaggerPath = swagger.getPath(swaggerPathString.original());

        final HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
        Operation operation = swaggerPath.getOperationMap().get(httpMethod);

        if (operation == null) {
            log.debug("Method '{}' is not defined for path '{}' in swagger documentation", request.getMethod(), requestPath.original());
            List<String> supportedMethods = swaggerPath.getOperationMap().keySet().stream().map(
                    Enum::name).collect(toList());
            throw new HttpRequestMethodNotSupportedException(request.getMethod(), supportedMethods);
        }
        return Optional.of(new SwaggerOperation(swaggerPathString, swaggerPath, httpMethod, operation));
    }

    private String getFieldFromJsonMessage(String message) {
        return message.split(":")[0];
    }

    private String buildErrorMessage(Object type, String fieldName, String reason) {
        String field = fieldName.contains("$.") ? StringUtils.substringAfterLast(fieldName, "$.") : fieldName;

        String message = String.format(reason, type, field);
        log.error("Validation error for field: {} with reason: {}", fieldName, message);
        return message;
    }
}
