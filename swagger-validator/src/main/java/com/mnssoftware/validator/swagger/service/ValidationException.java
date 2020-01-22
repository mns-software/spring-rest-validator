package com.mnssoftware.validator.swagger.service;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * <p>Thrown when there is a JSON schema validation error</p>
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 3803415613233272388L;
    private final List<String> validationErrors;

    public ValidationException(List<String> validationErrors) {
        super("[" + StringUtils.join(validationErrors, ",") + "]");
        this.validationErrors = validationErrors;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

}
