package com.mnssoftware.validator.swagger.service.parameter;

import com.mnssoftware.validator.swagger.service.ValidatorTestUtil;
import com.networknt.schema.ValidationMessage;
import io.swagger.models.parameters.SerializableParameter;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseParameterValidatorTest {

    @Test
    public void validate_notSupported_successful() throws Exception {
        assertThat(new TestParameterValidator().validate("", ValidatorTestUtil.intParam()), empty());
    }

    @Test
    public void validate_enumNotMatched_fails() throws Exception {
        final SerializableParameter param = mock(SerializableParameter.class);
        when(param.getName()).thenReturn("Test Parameter");
        when(param.getType()).thenReturn("string");
        when(param.getRequired()).thenReturn(true);
        when(param.getEnum()).thenReturn(asList("cow", "bull"));

        Set<ValidationMessage> messages = new TestParameterValidator().validate("sheep", param);

        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1008"));
    }

    @Test
    public void validate_enumMatched_successful() throws Exception {
        final SerializableParameter param = mock(SerializableParameter.class);
        when(param.getName()).thenReturn("Test Parameter");
        when(param.getType()).thenReturn("string");
        when(param.getRequired()).thenReturn(true);
        when(param.getEnum()).thenReturn(asList("cow", "bull"));

        assertThat(new TestParameterValidator().validate("cow", param), empty());
    }

    static class TestParameterValidator extends BaseParameterValidator {

        @Override
        public String supportedParameterType() {
            return "string";
        }

        @Override
        protected Set<ValidationMessage> doValidate(String value, SerializableParameter parameter) {
            return Collections.emptySet();
        }

    }
}
