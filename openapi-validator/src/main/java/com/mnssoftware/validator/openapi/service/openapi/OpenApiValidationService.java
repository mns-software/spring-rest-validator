package com.mnssoftware.validator.openapi.service.openapi;

import com.mnssoftware.validator.openapi.service.ValidationException;
import com.mnssoftware.validator.openapi.service.ValidationKeyMessage;
import com.mnssoftware.validator.openapi.service.ValidationService;
import com.networknt.oas.OpenApiParser;
import com.networknt.oas.model.OpenApi3;
import com.networknt.oas.model.Operation;
import com.networknt.oas.model.Path;
import com.networknt.schema.ValidationMessage;
import io.swagger.models.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Slf4j
public class OpenApiValidationService implements ValidationService {

  private final OpenApi3 openApi3;

  private RequestValidator requestValidator;

  public OpenApiValidationService(String openApiDocLocation) {
    try {
      openApi3 = (OpenApi3) new OpenApiParser().parse(openApiDocLocation, new URL("https://oas.lightapi.net/"));
      final SchemaValidator schemaValidator = new SchemaValidator(openApi3);
      this.requestValidator = new RequestValidator(schemaValidator);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Unable to parse OpenApi document: " + openApiDocLocation);
    }
  }

  @Override
  public void validateRequest(HttpServletRequest request) throws ServletException {
    final NormalisedPath requestPath = new ApiNormalisedPath(request.getRequestURI());
    Optional<OpenApiOperation> openApiOperation = getOpenApiOperation(request);

    openApiOperation.ifPresent(op -> {
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

  private Optional<OpenApiOperation> getOpenApiOperation(HttpServletRequest request) throws ServletException {
    final NormalisedPath requestPath = new ApiNormalisedPath(request.getRequestURI());
    final Optional<NormalisedPath> maybeApiPath = OpenApiHelper.findMatchingApiPath(openApi3, requestPath);
    if (!maybeApiPath.isPresent()) {
      log.debug("Path '{}' is not defined in openApi documentation", requestPath.original());
      return Optional.empty();
    }

    final NormalisedPath openApiPathString = maybeApiPath.get();
    final Path openApiPath = openApi3.getPath(openApiPathString.original());

    final HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
    Operation operation = openApiPath.getOperations().get(httpMethod);

    if (operation == null) {
      log.debug("Method '{}' is not defined for path '{}' in openApi documentation", request.getMethod(), requestPath.original());
      List<String> supportedMethods = openApiPath.getOperations().keySet().stream().collect(toList());
      throw new HttpRequestMethodNotSupportedException(request.getMethod(), supportedMethods);
    }
    return Optional.of(new OpenApiOperation(openApiPathString, openApiPath, httpMethod.name(), operation));
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
