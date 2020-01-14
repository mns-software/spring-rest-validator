package com.mnssoftware.validator.service.parameter;

import com.networknt.schema.ValidationMessage;
import io.swagger.models.parameters.Parameter;

import java.util.Set;

/**
 * Parameter validator interface. All validators must implement it.
 *
 * @author msilcox
 */
public interface ParameterValidator {

  String supportedParameterType();

  boolean supports(Parameter p);

  Set<ValidationMessage> validate(String value, Parameter p);
}
