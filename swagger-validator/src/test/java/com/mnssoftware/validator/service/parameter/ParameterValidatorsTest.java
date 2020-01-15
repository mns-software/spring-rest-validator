package com.mnssoftware.validator.service.parameter;

import com.networknt.schema.ValidationMessage;
import io.swagger.models.parameters.BodyParameter;
import org.junit.Test;

import java.util.Set;

import static com.mnssoftware.validator.service.ValidatorTestUtil.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class ParameterValidatorsTest {

  private final ParameterValidators parameterValidators = new ParameterValidators(null);

  @Test
  public void validate_withInvalidIntegerParam_shouldFail() {
    Set<ValidationMessage> messages = parameterValidators.validate("1.0", intParam());
    assertThat(messages, not(empty()));
    assertThat(messages.iterator().next().getCode(), equalTo("1029"));
  }

  @Test
  public void validate_withValidIntegerParam_shouldPass() {
    assertThat(parameterValidators.validate("10", intParam()), empty());
  }

  @Test
  public void validate_withInvalidNumberParam_shouldFail() {
    Set<ValidationMessage> messages = parameterValidators.validate("1.0a", floatParam());
    assertThat(messages, not(empty()));
    assertThat(messages.iterator().next().getCode(), equalTo("1029"));
  }

  @Test
  public void validate_withValidNumberParam_shouldPass() {
    assertThat(parameterValidators.validate("1.0", floatParam()), empty());
  }

  @Test
  public void validate_withInvalidStringParam_shouldFail() {
    Set<ValidationMessage> messages = parameterValidators.validate("", stringParam());
    assertThat(messages, not(empty()));
    assertThat(messages.iterator().next().getCode(), equalTo("1028"));
  }

  @Test
  public void validate_withValidStringParam_shouldPass() {
    assertThat(parameterValidators.validate("a", stringParam()), empty());
  }

  @Test
  public void validate_withValidArrayParam_shouldPass() {
    assertThat(parameterValidators.validate("1,2,3", intArrayParam(true, "csv")), empty());
  }

  @Test
  public void validate_withNonSerializableParam_shouldPass() {
    assertThat(parameterValidators.validate("1,2,3", new BodyParameter()), empty());
  }

}
