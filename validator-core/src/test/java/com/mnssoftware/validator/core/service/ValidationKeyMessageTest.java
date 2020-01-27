package com.mnssoftware.validator.core.service;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ValidationKeyMessageTest {

    @Test
    public void fromCode_valid_found() {
        assertThat(ValidationKeyMessage.getMessageFromCode("1028"), equalTo("The %s %s is mandatory"));
        assertThat(ValidationKeyMessage.getMessageFromCode("1029"), equalTo("The %s %s does not have the correct type"));
        assertThat(ValidationKeyMessage.getMessageFromCode("1023"), equalTo("The %s %s does not have the correct pattern"));
        assertThat(ValidationKeyMessage.getMessageFromCode("1008"), equalTo("The %s %s does not have the correct enum value"));
        assertThat(ValidationKeyMessage.getMessageFromCode("1013"), equalTo("The %s %s has an incorrect size"));
        assertThat(ValidationKeyMessage.getMessageFromCode("1017"), equalTo("The %s %s has an incorrect size"));
        assertThat(ValidationKeyMessage.getMessageFromCode("1012"), equalTo("The %s %s has too many items"));
        assertThat(ValidationKeyMessage.getMessageFromCode("1016"), equalTo("The %s %s has too few items"));
        assertThat(ValidationKeyMessage.getMessageFromCode("1015"), equalTo("The %s %s is below minimum allowed value"));
        assertThat(ValidationKeyMessage.getMessageFromCode("1011"), equalTo("The %s %s is above maximum allowed value"));
        assertThat(ValidationKeyMessage.getMessageFromCode("1031"), equalTo("The %s %s does not allow duplicate values"));
        assertThat(ValidationKeyMessage.getMessageFromCode("9001"), equalTo("No request body is expected but one was found"));
        assertThat(ValidationKeyMessage.getMessageFromCode("9002"), equalTo("Request body is expected but not found"));
    }

    @Test
    public void fromCode_invalid_defaulted() {
        assertThat(ValidationKeyMessage.getMessageFromCode("1111"), equalTo("The payload could not be parsed"));
    }
}