package com.mnssoftware.validator.swagger.service.parameter;

import com.networknt.schema.ValidationMessage;
import io.swagger.models.parameters.SerializableParameter;
import org.junit.Test;

import java.util.Set;

import static com.mnssoftware.validator.swagger.service.ValidatorTestUtil.stringParam;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class StringParameterValidatorTest {

    private StringParameterValidator classUnderTest = new StringParameterValidator();

    @Test
    public void validate_withNullValue_shouldPass_whenNotRequired() {
        assertThat(classUnderTest.validate(null, stringParam(false)), empty());
    }

    @Test
    public void validate_withEmptyValue_shouldPass_whenNotRequired() {
        assertThat(classUnderTest.validate("", stringParam(false)), empty());
    }

    @Test
    public void validate_withValue_shouldPass() {
        assertThat(classUnderTest.validate("hello", stringParam(true)), empty());
    }

    @Test
    public void validate_withNullValue_shouldFail_whenRequired() {
        Set<ValidationMessage> messages = classUnderTest.validate(null, stringParam(true));
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1028"));
    }

    @Test
    public void validate_withEmptyValue_shouldFail_whenRequired() {
        Set<ValidationMessage> messages = classUnderTest.validate("", stringParam(true));
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1028"));
    }

    @Test
    public void validate_withValue_shouldPass_whenPatternMatches() {
        assertThat(classUnderTest.validate("hello", stringParamWithPattern("[a-z]*")), empty());
    }

    @Test
    public void validate_withValue_shouldFail_whenPatternNotMatches() {
        Set<ValidationMessage> messages = classUnderTest.validate("hello", stringParamWithPattern("[0-9]*"));
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1023"));
    }

    private SerializableParameter stringParamWithPattern(String pattern) {
        SerializableParameter stringParam = stringParam(true);
        when(stringParam.getPattern()).thenReturn(pattern);
        return stringParam;
    }
}
