package com.mnssoftware.validator.swagger.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mnssoftware.validator.swagger.service.swagger.SwaggerHelper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import io.swagger.models.Model;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Json;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static com.mnssoftware.validator.swagger.service.ValidationKeyMessage.DEFAULT;
import static com.mnssoftware.validator.swagger.service.ValidationKeyMessage.NOT_NULL;
import static java.util.Objects.requireNonNull;

/**
 * Validate a value against the schema defined in a Swagger/OpenAPI specification.
 * <p>
 * Supports validation of properties and request/response bodies, and supports schema references.
 *
 * @author msilcox
 */
public class SchemaValidator {
    private static final String DEFINITIONS_FIELD = "definitions";

    private final Swagger api;
    private JsonNode definitions;

    /**
     * Build a new validator with no API specification.
     * <p>
     * This will not perform any validation of $ref references that reference local definitions.
     */
    public SchemaValidator() {
        this(null);
    }

    /**
     * Build a new validator for the given API specification.
     *
     * @param api The API to build the validator for. If provided, is used to retrieve schema definitions
     *            for use in references.
     */
    public SchemaValidator(final Swagger api) {
        this.api = api;
    }

    /**
     * Validate the given value against the given model schema.
     *
     * @param value  The value to validate
     * @param schema The model schema to validate the value against
     * @return A status containing error code and description
     */
    public Set<ValidationMessage> validate(final Object value, final Model schema) {
        requireNonNull(schema, "A schema is required");

        try {
            JsonSchema jsonSchema = getJsonSchema(Json.pretty(schema));

            final JsonNode content = Json.mapper().valueToTree(value);
            return jsonSchema.validate(content);
        } catch (IOException e) {
            return Collections.singleton(SwaggerHelper.buildValidationMessage(DEFAULT.getCode(), null));
        }
    }

    /**
     * Validate the given value against the given property schema.
     *
     * @param value  The value to validate
     * @param schema The property schema to validate the value against
     * @return A status containing error code and description
     */
    public Set<ValidationMessage> validate(final String value, final Property schema) {
        requireNonNull(schema, "A schema is required");

        try {
            JsonSchema jsonSchema = getJsonSchema(Json.pretty(schema));

            String normalisedValue = value;
            if (schema instanceof StringProperty) {
                normalisedValue = quote(value);
            }

            if (normalisedValue == null) {
                if (schema.getRequired()) {
                    return Collections.singleton(SwaggerHelper.buildValidationMessage(NOT_NULL.getCode(), schema.getName()));
                } else
                    return Collections.emptySet();
            }
            final JsonNode content = Json.mapper().readTree(normalisedValue);
            return jsonSchema.validate(content);
        } catch (IOException e) {
            return Collections.singleton(SwaggerHelper.buildValidationMessage(DEFAULT.getCode(), null));
        }
    }

    /**
     * Quote the given string if needed
     *
     * @param value The value to quote (e.g. bob)
     * @return The quoted string (e.g. "bob")
     */
    public static String quote(final String value) {
        if (value == null) {
            return null;
        }
        String result = value;
        if (!result.startsWith("\"")) {
            result = "\"" + result;
        }
        if (!result.endsWith("\"")) {
            result = result + "\"";
        }
        return result;
    }

    private JsonSchema getJsonSchema(String schema) throws IOException {
        final JsonNode schemaObject = Json.mapper().readTree(schema);

        if (api != null) {
            if (this.definitions == null) {
                this.definitions = Json.mapper().readTree(Json.pretty(api.getDefinitions()));
            }
            ((ObjectNode) schemaObject).set(DEFINITIONS_FIELD, this.definitions);
        }
        SchemaValidatorsConfig defaultConfig = new SchemaValidatorsConfig();
        defaultConfig.setTypeLoose(true);
        return JsonSchemaFactory.getInstance().getSchema(schemaObject, defaultConfig);
    }
}
