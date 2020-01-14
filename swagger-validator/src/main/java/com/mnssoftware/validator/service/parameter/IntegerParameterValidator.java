package com.mnssoftware.validator.service.parameter;

import com.networknt.schema.ValidationMessage;
import io.swagger.models.parameters.SerializableParameter;

import java.util.Collections;
import java.util.Set;

import static com.mnssoftware.validator.service.ValidationKeyMessage.*;
import static com.mnssoftware.validator.service.swagger.SwaggerHelper.buildValidationMessage;

/**
 * Integer parameter validator
 *
 * @author msilcox
 */
public class IntegerParameterValidator extends BaseParameterValidator {

  public IntegerParameterValidator() {
    super();
  }

  @Override
  public String supportedParameterType() {
    return "integer";
  }

  @Override
  protected Set<ValidationMessage> doValidate(final String value, final SerializableParameter parameter) {

    if (parameter.getFormat().equalsIgnoreCase("int32")) {
      try {
        Integer.parseInt(value);
      } catch (NumberFormatException e) {
        return failFormatValidation(parameter);
      }
    } else if (parameter.getFormat().equalsIgnoreCase("int64")) {
      try {
        Long.parseLong(value);
      } catch (NumberFormatException e) {
        return failFormatValidation(parameter);
      }
    } else {
      // Should never get here, but for completeness
      throw new IllegalArgumentException("Unable to validate parameter with format " + parameter.getFormat());
    }

    final long d = Long.parseLong(value);
    if (parameter.getMinimum() != null && d < parameter.getMinimum().longValue()) {
      return Collections.singleton(buildValidationMessage(MIN_VALUE.getCode(), parameter.getName()));
    }

    if (parameter.getMaximum() != null && d > parameter.getMaximum().longValue()) {
      return Collections.singleton(buildValidationMessage(MAX_VALUE.getCode(), parameter.getName()));
    }
    return Collections.emptySet();
  }

  private Set<ValidationMessage> failFormatValidation(final SerializableParameter parameter) {
    return Collections.singleton(buildValidationMessage(TYPE.getCode(), parameter.getName()));
  }
}
