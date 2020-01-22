/*
 * Copyright (c) 2019 MNS Software Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mnssoftware.validator.openapi.service.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.config.Config;
import com.networknt.jsonoverlay.Overlay;
import com.networknt.oas.model.OpenApi3;
import com.networknt.oas.model.impl.OpenApi3Impl;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;

import java.util.Collections;
import java.util.Set;

import static com.mnssoftware.validator.openapi.service.ValidationKeyMessage.DEFAULT;
import static java.util.Objects.requireNonNull;

/**
 * Validate a value against the schema defined in an OpenAPI specification.
 * <p>
 * Supports validation of properties and request/response bodies, and supports schema references.
 *
 * @author Mark Silcox
 */
public class SchemaValidator {
    private static final String COMPONENTS_FIELD = "components";

    private final OpenApi3 api;
    private JsonNode jsonNode;
    private final SchemaValidatorsConfig defaultConfig;

    /**
     * Build a new validator with no API specification.
     * <p>
     * This will not perform any validation of $ref references that reference local schemas.
     */
    public SchemaValidator() {
        this(null);
    }

    /**
     * Build a new validator for the given API specification.
     *
     * @param api The API to build the validator for. If provided, is used to retrieve schemas in components
     *            for use in references.
     */
    public SchemaValidator(final OpenApi3 api) {
        this.api = api;
        this.jsonNode = Overlay.toJson((OpenApi3Impl) api).get("components");
        this.defaultConfig = new SchemaValidatorsConfig();
        this.defaultConfig.setTypeLoose(true);
    }

    /**
     * Validate the given value against the given property schema.
     *
     * @param value  The value to validate
     * @param schema The property schema to validate the value against
     * @param config The config model for some validator
     * @return A status containing error code and description
     */
    public Set<ValidationMessage> validate(final Object value, final JsonNode schema, SchemaValidatorsConfig config) {
        return doValidate(value, schema, config);
    }

    public Set<ValidationMessage> validate(final Object value, final JsonNode schema) {
        return doValidate(value, schema, defaultConfig);
    }

    private Set<ValidationMessage> doValidate(final Object value, final JsonNode schema, SchemaValidatorsConfig config) {
        requireNonNull(schema, "A schema is required");

        try {
            if (jsonNode != null) {
                ((ObjectNode) schema).set(COMPONENTS_FIELD, jsonNode);
            }
            JsonSchema jsonSchema = JsonSchemaFactory.getInstance().getSchema(schema, config);
            final JsonNode content = Config.getInstance().getMapper().valueToTree(value);
            return jsonSchema.validate(content);
        } catch (Exception e) {
            return Collections.singleton(OpenApiHelper.buildValidationMessage(DEFAULT.getCode(), null));
        }
    }
}
