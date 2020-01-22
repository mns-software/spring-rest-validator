package com.mnssoftware.validator.swagger.service.parameter;

import com.networknt.schema.ValidationMessage;
import io.swagger.models.parameters.SerializableParameter;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static com.mnssoftware.validator.swagger.service.ValidatorTestUtil.doubleParam;
import static com.mnssoftware.validator.swagger.service.ValidatorTestUtil.floatParam;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class NumberParameterValidatorTest {

    private NumberParameterValidator classUnderTest = new NumberParameterValidator();

    @Test
    public void validate_withNullValue_shouldPass_whenNotRequired() {
        assertThat(classUnderTest.validate(null, floatParam(false)), empty());
    }

    @Test
    public void validate_withEmptyValue_shouldPass_whenNotRequired() {
        assertThat(classUnderTest.validate("", floatParam(false)), empty());
    }

    @Test
    public void validate_withNullValue_shouldFail_whenRequired() {
        Set<ValidationMessage> messages = classUnderTest.validate(null, floatParam(true));
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1028"));
    }

    @Test
    public void validate_withEmptyValue_shouldFail_whenRequired() {
        Set<ValidationMessage> messages = classUnderTest.validate("", floatParam(true));
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1028"));
    }

    @Test
    public void validate_withNonNumericFloatValue_shouldFail() {
        Set<ValidationMessage> messages = classUnderTest.validate("not-a-Number", floatParam());
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1029"));
    }

    @Test
    public void validate_withNonNumericDoubleValue_shouldFail() {
        Set<ValidationMessage> messages = classUnderTest.validate("not-a-Number", doubleParam());
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1029"));
    }

    @Test
    public void validate_withPositiveNumericValue_shouldPass() {
        assertThat(classUnderTest.validate("123.456", doubleParam()), empty());
    }

    @Test
    public void validate_withNegativeNumericValue_shouldPass() {
        assertThat(classUnderTest.validate("-123.456", doubleParam()), empty());
    }

    @Test
    public void validate_withValueGreaterThanMax_shouldFail_ifMaxSpecified() {
        Set<ValidationMessage> messages = classUnderTest.validate("1.1", floatParam(null, new BigDecimal(1.0)));
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1011"));
    }

    @Test
    public void validate_withValueLessThanMin_shouldFail_ifMinSpecified() {
        Set<ValidationMessage> messages = classUnderTest.validate("0.9", floatParam(new BigDecimal(1.0), null));
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1015"));
    }

    @Test
    public void validate_withValueInRange_shouldPass() {
        assertThat(classUnderTest.validate("1.1", floatParam(new BigDecimal(1.0), new BigDecimal(1.2))), empty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_withNonNumberParameter_shouldPass() {
        SerializableParameter param = doubleParam();
        when(param.getFormat()).thenReturn("integer");
        classUnderTest.validate("1.1", param);
    }
}
