package com.mnssoftware.validator.service.parameter;

import com.mnssoftware.validator.service.SchemaValidator;
import com.networknt.schema.ValidationMessage;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.mnssoftware.validator.service.ValidationKeyMessage.*;
import static com.mnssoftware.validator.service.swagger.SwaggerHelper.buildValidationMessage;
import static java.util.Objects.requireNonNull;

/**
 * A validator for array parameters.
 *
 * @author msilcox
 */
public class ArrayParameterValidator extends BaseParameterValidator {

  public static final String ARRAY_PARAMETER_TYPE = "array";

  private final SchemaValidator schemaValidator;

  private enum CollectionFormat {
    CSV(","),
    SSV(" "),
    TSV("\t"),
    PIPES("\\|"),
    MULTI(null);

    final String separator;

    CollectionFormat(String separator) {
      this.separator = separator;
    }

    Collection<String> split(final String value) {
      if (separator == null) {
        return Collections.singleton(value);
      }
      return Arrays.asList(value.split(separator));
    }

    static CollectionFormat from(final SerializableParameter parameter) {
      requireNonNull(parameter, "A parameter is required");
      return valueOf(parameter.getCollectionFormat().toUpperCase());
    }
  }

  public ArrayParameterValidator(final SchemaValidator schemaValidator) {
    this.schemaValidator = schemaValidator == null ? new SchemaValidator() : schemaValidator;
  }

  @Override
  public String supportedParameterType() {
    return ARRAY_PARAMETER_TYPE;
  }

  @Override
  public Set<ValidationMessage> validate(final String value, final Parameter p) {

    if (!supports(p)) {
      return Collections.emptySet();
    }

    final SerializableParameter parameter = (SerializableParameter) p;

    if (parameter.getRequired() && (value == null || value.trim().isEmpty())) {
      return Collections.singleton(buildValidationMessage(NOT_NULL.getCode(), parameter.getName()));
    }

    if (value == null || value.trim().isEmpty()) {
      return Collections.emptySet();
    }

    return Optional.ofNullable(doValidate(value, parameter)).orElse(Collections.emptySet());
  }

  @Override
  protected Set<ValidationMessage> doValidate(final String value, final SerializableParameter parameter) {
    return doValidate(CollectionFormat.from(parameter).split(value), parameter);
  }

  private Set<ValidationMessage> doValidate(final Collection<String> values,
                                            final SerializableParameter parameter) {

    if (parameter.getMaxItems() != null && values.size() > parameter.getMaxItems()) {
      return Collections.singleton(buildValidationMessage(MAX_ITEMS.getCode(), parameter.getName()));
    }

    if (parameter.getMinItems() != null && values.size() < parameter.getMinItems()) {
      return Collections.singleton(buildValidationMessage(MIN_ITEMS.getCode(), parameter.getName()));
    }

    if (Boolean.TRUE.equals(parameter.isUniqueItems()) &&
            values.stream().distinct().count() != values.size()) {
      return Collections.singleton(buildValidationMessage(DUPLICATES.getCode(), parameter.getName()));
    }

    if (!CollectionUtils.isEmpty(parameter.getEnum())) {
      final Set<String> enumValues = new HashSet<>(parameter.getEnum());
      Optional<String> value =
              values.stream()
                      .filter(v -> !enumValues.contains(v))
                      .findFirst();
      if (value.isPresent()) {
        return Collections.singleton(buildValidationMessage(ENUM.getCode(), parameter.getName()));
      }
    }

    return
            values.stream()
                    .flatMap(v -> schemaValidator.validate(v, parameter.getItems()).stream()).collect(Collectors.toSet());
  }
}
