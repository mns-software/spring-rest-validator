package com.mnssoftware.validator.swagger.service.parameter;

import com.mnssoftware.validator.swagger.service.swagger.SwaggerHelper;
import com.networknt.schema.ValidationMessage;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.mnssoftware.validator.swagger.service.ValidationKeyMessage.ENUM;
import static com.mnssoftware.validator.swagger.service.ValidationKeyMessage.NOT_NULL;

/**
 * An abstract implementation of ParameterValidator.
 *
 * @author msilcox
 */
abstract class BaseParameterValidator implements ParameterValidator {

    protected BaseParameterValidator() {

    }

    @Override
    public boolean supports(final Parameter p) {
        return p instanceof SerializableParameter && supportedParameterType().equalsIgnoreCase(((SerializableParameter) p).getType());
    }

    @Override
    public Set<ValidationMessage> validate(final String value, final Parameter p) {

        if (!supports(p)) {
            return Collections.emptySet();
        }

        final SerializableParameter parameter = (SerializableParameter) p;

        if (parameter.getRequired() && (value == null || value.trim().isEmpty())) {
            return Collections.singleton(SwaggerHelper.buildValidationMessage(NOT_NULL.getCode(), parameter.getName()));
        }

        if (value == null || value.trim().isEmpty()) {
            return Collections.emptySet();
        }

        if (!matchesEnumIfDefined(value, parameter)) {
            return Collections.singleton(SwaggerHelper.buildValidationMessage(ENUM.getCode(), parameter.getName()));
        }
        return Optional.ofNullable(doValidate(value, parameter)).orElse(Collections.emptySet());
    }

    private boolean matchesEnumIfDefined(final String value, final SerializableParameter parameter) {
        return CollectionUtils.isEmpty(parameter.getEnum()) || parameter.getEnum().contains(value);
    }

    /**
     * Perform type-specific validations
     *
     * @param value     The value being validated
     * @param parameter The parameter the value is being validated against
     * @return ValidationMessage The status object or null if there is no error
     */
    protected abstract Set<ValidationMessage> doValidate(String value, SerializableParameter parameter);
}
