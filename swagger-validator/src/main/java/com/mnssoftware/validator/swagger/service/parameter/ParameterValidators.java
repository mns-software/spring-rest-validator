package com.mnssoftware.validator.swagger.service.parameter;

import com.mnssoftware.validator.swagger.service.SchemaValidator;
import com.networknt.schema.ValidationMessage;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;

import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

/**
 * An aggregated validators
 *
 * @author msilcox
 */
public final class ParameterValidators {

    private final ArrayParameterValidator arrayValidator;
    private final List<ParameterValidator> validators;

    /**
     * Create a new validators object with the given schema validator. If none is provided a default (empty) schema
     * validator will be used and no <code>ref</code> validation will be performed.
     *
     * @param schemaValidator The schema validator to use. If not provided a default (empty) validator will be used.
     */
    public ParameterValidators(final SchemaValidator schemaValidator) {
        this.arrayValidator = new ArrayParameterValidator(schemaValidator);
        this.validators = asList(
                new StringParameterValidator(),
                new NumberParameterValidator(),
                new IntegerParameterValidator()
        );
    }

    public Set<ValidationMessage> validate(final String value, final Parameter parameter) {
        requireNonNull(parameter);

        if ((parameter instanceof SerializableParameter) &&
                ((SerializableParameter) parameter).getType().equalsIgnoreCase("array")) {
            return arrayValidator.validate(value, parameter);
        }

        return validators.stream()
                .filter(v -> v.supports(parameter))
                .flatMap(v -> v.validate(value, parameter).stream())
                .collect(toSet());
    }

}
