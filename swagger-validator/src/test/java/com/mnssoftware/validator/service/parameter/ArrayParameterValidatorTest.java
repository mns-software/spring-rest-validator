package com.mnssoftware.validator.service.parameter;

import com.mnssoftware.validator.service.SchemaValidator;
import com.networknt.schema.ValidationMessage;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.properties.IntegerProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;

import static com.mnssoftware.validator.service.ValidatorTestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ArrayParameterValidatorTest {

  @Mock
  private SchemaValidator schemaValidator;

  private ArrayParameterValidator classUnderTest;

  @Before
  public void setUp() {
    classUnderTest = new ArrayParameterValidator(null);
  }

  @Test
  public void validate_withValidator_shouldPass() {
    ArrayParameterValidator withValidator = new ArrayParameterValidator(schemaValidator);

    assertThat(withValidator.validate("1,2,3", intArrayParam(true, "csv")), empty());
  }

  @Test
  public void validate_withValidCsvFormat_shouldPass() {
    assertThat(classUnderTest.validate("1,2,3", intArrayParam(true, "csv")), empty());
  }

  @Test
  public void validate_withValidPipesFormat_shouldPass() {
    assertThat(classUnderTest.validate("1|2|3", intArrayParam(true, "pipes")), empty());
  }

  @Test
  public void validate_withValidTsvFormat_shouldPass() {
    assertThat(classUnderTest.validate("1\t2\t3", intArrayParam(true, "tsv")), empty());
  }

  @Test
  public void validate_withValidSsvFormat_shouldPass() {
    assertThat(classUnderTest.validate("1 2 3", intArrayParam(true, "ssv")), empty());
  }

  @Test
  public void validate_withValidMultiFormat_shouldPass() {
    assertThat(classUnderTest.validate("123", intArrayParam(true, "multi")), empty());
  }

  @Test
  public void validate_withTrailingSeparator_shouldPass() {
    assertThat(classUnderTest.validate("1,2,3,", intArrayParam(true, "csv")), empty());
  }

  @Test
  public void validate_withSingleValue_shouldPass() {
    assertThat(classUnderTest.validate("bob", stringArrayParam(true, "csv")), empty());
  }

  @Test
  public void validate_notSupported_shouldPass() {
    assertThat(classUnderTest.validate("1,2,3", new BodyParameter()), empty());
  }

  @Test
  public void validate_withInvalidParameter_shouldFail() {
    Set<ValidationMessage> messages = classUnderTest.validate("1,2.1,3", intArrayParam(true, "csv"));
    assertThat(messages, not(empty()));
    assertThat(messages.iterator().next().getCode(), equalTo("1029"));
  }

  @Test
  public void validate_withEmptyValue_shouldFail_whenRequired() {
    Set<ValidationMessage> messages = classUnderTest.validate("", intArrayParam(true, "csv"));
    assertThat(messages, not(empty()));
    assertThat(messages.iterator().next().getCode(), equalTo("1028"));
  }

  @Test
  public void validate_withNullValue_shouldFail_whenRequired() {
    Set<ValidationMessage> messages = classUnderTest.validate((String) null, intArrayParam(true, "csv"));
    assertThat(messages, not(empty()));
    assertThat(messages.iterator().next().getCode(), equalTo("1028"));
  }

  @Test
  public void validate_withEmptyValue_shouldPass_whenNotRequired() {
    assertThat(classUnderTest.validate("", intArrayParam(false, "csv")), empty());
  }

  @Test
  public void validate_withNullValue_shouldPass_whenNotRequired() {
    assertThat(classUnderTest.validate((String) null, intArrayParam(false, "csv")), empty());
  }

  @Test
  public void validate_withEnoughValues_shouldPass_whenMinMaxItemsSpecified() {
    assertThat(classUnderTest.validate("1,2,3,4", arrayParam(true, "csv", 3, 5, null, new IntegerProperty())), empty());
  }

  @Test
  public void validate_withTooFewValues_shouldFail_whenMinItemsSpecified() {
    Set<ValidationMessage> messages = classUnderTest.validate("1,2", arrayParam(true, "csv", 3, 5, null, new IntegerProperty()));
    assertThat(messages, not(empty()));
    assertThat(messages.iterator().next().getCode(), equalTo("1016"));
  }

  @Test
  public void validate_withTooManyValues_shouldFail_whenMaxItemsSpecified() {
    Set<ValidationMessage> messages = classUnderTest.validate("1,2,3,4,5,6", arrayParam(true, "csv", 3, 5, null, new IntegerProperty()));
    assertThat(messages, not(empty()));
    assertThat(messages.iterator().next().getCode(), equalTo("1012"));
  }

  @Test
  public void validate_withNonUniqueValues_shouldFail_whenUniqueSpecified() {
    Set<ValidationMessage> messages = classUnderTest.validate("1,2,1", arrayParam(true, "csv", null, null, true, new IntegerProperty()));
    assertThat(messages, not(empty()));
    assertThat(messages.iterator().next().getCode(), equalTo("1031"));
  }

  @Test
  public void validate_withUniqueValues_shouldPass_whenUniqueSpecified() {
    assertThat(classUnderTest.validate("1,2,3", arrayParam(true, "csv", null, null, true, new IntegerProperty())), empty());
  }

  @Test
  public void validate_withNonUniqueValues_shouldPass_whenUniqueNotSpecified() {
    assertThat(classUnderTest.validate("1,2,1", arrayParam(true, "csv", null, null, false, new IntegerProperty())), empty());
  }

  @Test
  public void validate_withEnumValues_shouldPass_whenAllValuesMatchEnum() {
    assertThat(classUnderTest.validate("1,2,1", enumeratedArrayParam(true, "csv", new IntegerProperty(), "1", "2", "3")), empty());
  }

  @Test
  public void validate_withEnumValues_shouldFail_whenValueDoesntMatchEnum() {
    Set<ValidationMessage> messages = classUnderTest.validate("1,2,1,4",
            enumeratedArrayParam(true, "csv", new IntegerProperty(), "1", "2", "bob"));
    assertThat(messages, not(empty()));
    assertThat(messages.iterator().next().getCode(), equalTo("1008"));
  }
}
