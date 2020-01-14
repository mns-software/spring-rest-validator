package com.mnssoftware.validator.service.parameter;

import com.networknt.schema.ValidationMessage;
import io.swagger.models.parameters.SerializableParameter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import static com.mnssoftware.validator.service.ValidationKeyMessage.PATTERN;
import static com.mnssoftware.validator.service.swagger.SwaggerHelper.buildValidationMessage;

/**
 * String type parameter validator
 *
 * @author msilcox
 */
public class StringParameterValidator extends BaseParameterValidator {

  public StringParameterValidator() {
    super();
  }

  @Override
  public String supportedParameterType() {
    return "string";
  }

  @Override
  protected Set<ValidationMessage> doValidate(final String value, final SerializableParameter parameter) {
    String decodedValue = UriUtils.decode(value, StandardCharsets.UTF_8);
    if (StringUtils.isNotEmpty(parameter.getPattern()) && !Pattern.matches(parameter.getPattern(), decodedValue)) {
      return Collections.singleton(buildValidationMessage(PATTERN.getCode(), parameter.getName()));
    }
    return Collections.emptySet();
  }
}
