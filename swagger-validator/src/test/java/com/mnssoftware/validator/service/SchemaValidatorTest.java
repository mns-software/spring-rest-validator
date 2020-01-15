package com.mnssoftware.validator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SchemaValidatorTest {

    @Mock
    private Swagger swagger;
    @Mock
    private JsonNode jsonNode;

    private Model model;
    private SchemaValidator underTest;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() {
        underTest = new SchemaValidator(swagger);
        model = new ModelImpl();
    }

    @Test
    public void validateModel_withSwagger_successful() {
        Set<ValidationMessage> messages = underTest.validate(jsonNode, model);

        assertThat(messages, empty());
    }

    @Test
    public void validateModel_nullSwagger_successful() {
        SchemaValidator underTest = new SchemaValidator();

        Set<ValidationMessage> messages = underTest.validate(jsonNode, model);

        assertThat(messages, empty());
    }

    @Test
    public void validateModel_withNullModel_throws() {
        expected.expect(NullPointerException.class);

        underTest.validate(jsonNode, null);
    }

    @Test
    public void validateModel_definitionsPopulated_successful() {

        underTest.validate(null, model);

        Set<ValidationMessage> messages = underTest.validate(null, model);

        assertThat(messages, empty());
    }

    @Test
    public void validateModel_withJsonException_fails() {
        when(swagger.getDefinitions()).thenAnswer(invocation -> {
            throw mock(JsonProcessingException.class);
        });

        Set<ValidationMessage> messages = underTest.validate(jsonNode, model);

        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("9999"));
    }

    @Test
    public void validateProperty_stringProperty_happyPath() {
        Set<ValidationMessage> messages = underTest.validate("hello", new StringProperty());

        assertThat(messages, empty());
    }

    @Test
    public void validateProperty_integerProperty_happyPath() {
        Set<ValidationMessage> messages = underTest.validate("123", new IntegerProperty());

        assertThat(messages, empty());
    }

    @Test
    public void validateProperty_withJsonException_fails() {
        when(swagger.getDefinitions()).thenAnswer(invocation -> {
            throw mock(JsonProcessingException.class);
        });

        Set<ValidationMessage> messages = underTest.validate("hello", new StringProperty());

        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("9999"));
    }

    @Test
    public void validateProperty_nullStringProperty_happyPath() {
        Set<ValidationMessage> messages = underTest.validate(null, new StringProperty());

        assertThat(messages, empty());
    }

    @Test
    public void validateProperty_nullStringPropertyRequired_fails() {
        StringProperty property = new StringProperty();
        property.setRequired(true);
        Set<ValidationMessage> messages = underTest.validate(null, property);

        assertThat(messages, not(empty()));
        assertThat(messages.iterator().next().getCode(), equalTo("1028"));
    }

    @Test
    public void validateProperty_quotedStringProperty_happyPath() {
        Set<ValidationMessage> messages = underTest.validate("\"hello\"", new StringProperty());

        assertThat(messages, empty());
    }

}