package com.mnssoftware.validator.openapi.service;

import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;

import java.math.BigDecimal;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidatorTestUtil {

    // Int parameters
    public static SerializableParameter intParam() {
        return intParam(true, null, null);
    }

    public static SerializableParameter intParam(boolean required) {
        return intParam(required, null, null);
    }

    public static SerializableParameter intParam(final BigDecimal min, final BigDecimal max) {
        return intParam(true, min, max);
    }

    public static SerializableParameter intParam(final boolean required, final BigDecimal min, final BigDecimal max) {
        final SerializableParameter result = mock(SerializableParameter.class);
        when(result.getName()).thenReturn("Test Parameter");
        when(result.getType()).thenReturn("integer");
        when(result.getFormat()).thenReturn("int32");
        when(result.getRequired()).thenReturn(required);
        when(result.getMinimum()).thenReturn(min);
        when(result.getMaximum()).thenReturn(max);
        return result;
    }

    // String parameters

    public static SerializableParameter stringParam() {
        return stringParam(true);
    }

    public static SerializableParameter stringParam(final boolean required) {
        return stringParam(required, "query");
    }

    public static SerializableParameter stringParam(final boolean required, final String in) {
        final SerializableParameter result = mock(SerializableParameter.class);
        when(result.getName()).thenReturn("Test Parameter");
        when(result.getType()).thenReturn("string");
        when(result.getIn()).thenReturn(in);
        when(result.getRequired()).thenReturn(required);
        return result;
    }

    // Double parameters

    public static SerializableParameter doubleParam() {
        return doubleParam(true, null, null);
    }

    public static SerializableParameter doubleParam(boolean required) {
        return doubleParam(required, null, null);
    }

    public static SerializableParameter doubleParam(final BigDecimal min, final BigDecimal max) {
        return doubleParam(true, min, max);
    }

    public static SerializableParameter doubleParam(final boolean required, final BigDecimal min, final BigDecimal max) {
        final SerializableParameter result = mock(SerializableParameter.class);
        when(result.getName()).thenReturn("Test Parameter");
        when(result.getType()).thenReturn("number");
        when(result.getFormat()).thenReturn("double");
        when(result.getRequired()).thenReturn(required);
        when(result.getMinimum()).thenReturn(min);
        when(result.getMaximum()).thenReturn(max);
        return result;
    }

    // Float parameters

    public static SerializableParameter floatParam() {
        return floatParam(true, null, null);
    }

    public static SerializableParameter floatParam(boolean required) {
        return floatParam(required, null, null);
    }

    public static SerializableParameter floatParam(final BigDecimal min, final BigDecimal max) {
        return floatParam(true, min, max);
    }

    public static SerializableParameter floatParam(final boolean required, final BigDecimal min, final BigDecimal max) {
        final SerializableParameter result = mock(SerializableParameter.class);
        when(result.getName()).thenReturn("Test Parameter");
        when(result.getType()).thenReturn("number");
        when(result.getFormat()).thenReturn("float");
        when(result.getRequired()).thenReturn(required);
        when(result.getMinimum()).thenReturn(min);
        when(result.getMaximum()).thenReturn(max);
        return result;
    }

    // Array parameters

    public static SerializableParameter intArrayParam(final boolean required, final String collectionFormat) {
        final IntegerProperty property = new IntegerProperty();
        return arrayParam(required, collectionFormat, null, null, null, property);
    }

    public static SerializableParameter stringArrayParam(final boolean required, final String collectionFormat) {
        final StringProperty property = new StringProperty();
        return arrayParam(required, collectionFormat, null, null, null, property);
    }

    public static SerializableParameter enumeratedArrayParam(final boolean required,
                                                             final String collectionFormat,
                                                             final Property items,
                                                             final String... enumValues) {
        final SerializableParameter result = mock(SerializableParameter.class);
        when(result.getName()).thenReturn("Test Parameter");
        when(result.getType()).thenReturn("array");
        when(result.getCollectionFormat()).thenReturn(collectionFormat);
        when(result.getRequired()).thenReturn(required);
        when(result.getMaxItems()).thenReturn(null);
        when(result.getMinItems()).thenReturn(null);
        when(result.getEnum()).thenReturn(asList(enumValues));
        when(result.getItems()).thenReturn(items);
        return result;
    }

    public static SerializableParameter arrayParam(final boolean required,
                                                   final String collectionFormat,
                                                   final Integer minItems,
                                                   final Integer maxItems,
                                                   final Boolean unique,
                                                   final Property items) {

        final SerializableParameter result = mock(SerializableParameter.class);
        when(result.getName()).thenReturn("Test Parameter");
        when(result.getType()).thenReturn("array");
        when(result.getCollectionFormat()).thenReturn(collectionFormat);
        when(result.getRequired()).thenReturn(required);
        when(result.getMinItems()).thenReturn(minItems);
        when(result.getMaxItems()).thenReturn(maxItems);
        when(result.isUniqueItems()).thenReturn(unique);
        when(result.getItems()).thenReturn(items);
        return result;
    }
}
