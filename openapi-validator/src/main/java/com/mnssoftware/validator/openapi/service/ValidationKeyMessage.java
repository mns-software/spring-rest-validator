package com.mnssoftware.validator.openapi.service;

import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * Enumeration to give abstraction from 3rd party {@link com.networknt.schema.ValidatorTypeCode}
 *
 * @author msilcox
 */
public enum ValidationKeyMessage {
    NOT_NULL("1028", "The %s %s is mandatory"),
    TYPE("1029", "The %s %s does not have the correct type"),
    PATTERN("1023", "The %s %s does not have the correct pattern"),
    ENUM("1008", "The %s %s does not have the correct enum value"),
    MAX_LENGTH("1013", "The %s %s has an incorrect size"),
    MIN_LENGTH("1017", "The %s %s has an incorrect size"),
    MAX_ITEMS("1012", "The %s %s has too many items"),
    MIN_ITEMS("1016", "The %s %s has too few items"),
    MIN_VALUE("1015", "The %s %s is below minimum allowed value"),
    MAX_VALUE("1011", "The %s %s is above maximum allowed value"),
    DUPLICATES("1031", "The %s %s does not allow duplicate values"),
    UNEXPECTED_BODY("9001", "No request body is expected but one was found"),
    MISSING_BODY("9002", "Request body is expected but not found"),
    DEFAULT("9999", "The payload could not be parsed");

    private final String code;
    private final String message;

    ValidationKeyMessage(String code, String message) {
        this.message = message;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static String getMessageFromCode(String code) {
        return Stream.of(ValidationKeyMessage.values()).filter(v -> StringUtils.equals(v.getCode(), code)).findFirst().map(
                ValidationKeyMessage::getMessage).orElse(DEFAULT.getMessage());
    }

}
