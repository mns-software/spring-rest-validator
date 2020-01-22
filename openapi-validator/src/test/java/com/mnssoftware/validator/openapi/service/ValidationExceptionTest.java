package com.mnssoftware.validator.openapi.service;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ValidationExceptionTest {

    @Test
    public void constructor_withErrors_createsMessage() {
        List<String> errorList = Arrays.asList("I am an error", "So am I!");

        ValidationException underTest = new ValidationException(errorList);

        assertThat(underTest.getMessage(), equalTo("[I am an error,So am I!]"));
    }

    @Test
    public void getValidationErrors_withErrors_successful() {
        List<String> errorList = Arrays.asList("I am an error", "So am I!");

        ValidationException underTest = new ValidationException(errorList);

        assertThat(underTest.getValidationErrors(), equalTo(errorList));
    }
}