package com.mnssoftware.validator.openapi.service.openapi.parameter;

import com.networknt.schema.ValidationMessage;
import io.swagger.models.parameters.SerializableParameter;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static com.mnssoftware.validator.openapi.service.ValidatorTestUtil.intParam;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class IntegerParameterValidatorTest {

    private final IntegerParameterValidator classUnderTest = new IntegerParameterValidator();

    @Test
    public void validate_withNullValue_shouldPass_whenNotRequired() {
        assertThat(classUnderTest.validate(null, intParam(false)), empty());
    }

    @Test
    public void validate_withEmptyValue_shouldPass_whenNotRequired() {
        assertThat(classUnderTest.validate("", intParam(false)), empty());
    }

    @Test
    public void validate_withLongValue_shouldPass_whenNotRequired() {
        SerializableParameter param = intParam(false);
        when(param.getFormat()).thenReturn("int64");
        assertThat(classUnderTest.validate("123", param), empty());
    }

    @Test
    public void validate_withNullValue_shouldFail_whenRequired() {
        Set<ValidationMessage> messages = classUnderTest.validate(null, intParam(true));
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1028"));
    }

    @Test
    public void validate_withEmptyValue_shouldFail_whenRequired() {
        Set<ValidationMessage> messages = classUnderTest.validate("", intParam(true));
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1028"));
    }

    @Test
    public void validate_withNonNumericValue_shouldFail() {
        Set<ValidationMessage> messages = classUnderTest.validate("123a", intParam(true));
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1029"));
    }

    @Test
    public void validate_withNonNumericLongValue_shouldFail() {
        SerializableParameter param = intParam(false);
        when(param.getFormat()).thenReturn("int64");
        Set<ValidationMessage> messages = classUnderTest.validate("123a", param);
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1029"));
    }

    @Test
    public void validate_withNonIntegerValue_shouldFail() {
        Set<ValidationMessage> messages = classUnderTest.validate("123.1", intParam(true));
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1029"));
    }

    @Test
    public void validate_withIntegerValue_shouldPass() {
        assertThat(classUnderTest.validate("123", intParam()), empty());
    }

    @Test
    public void validate_withValueGreaterThanMax_shouldFail_ifMaxSpecified() {
        Set<ValidationMessage> messages = classUnderTest.validate("2", intParam(null, new BigDecimal(1.0)));
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1011"));
    }

    @Test
    public void validate_withValueLessThanMin_shouldFail_ifMinSpecified() {
        Set<ValidationMessage> messages = classUnderTest.validate("0", intParam(new BigDecimal(1.0), null));
        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1015"));
    }

    @Test
    public void validate_withValueInRange_shouldPass() {
        assertThat(classUnderTest.validate("2", intParam(new BigDecimal(1.0), new BigDecimal(3.0))), empty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_withNonNumberParameter_shouldPass() {
        SerializableParameter param = intParam();
        when(param.getFormat()).thenReturn("float");
        classUnderTest.validate("1", param);
    }

}
