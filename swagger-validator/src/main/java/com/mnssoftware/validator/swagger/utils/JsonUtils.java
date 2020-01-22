package com.mnssoftware.validator.swagger.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

@Slf4j
public final class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtils() {
        // private constructor to hide implicit public one
    }

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Write given object to json string
     *
     * @param obj Object to convert
     * @return Optional String JSON representation of the object or Optional.empty() if unable to read
     */
    public static Optional<String> toString(Object obj) {
        try {
            return Optional.of(OBJECT_MAPPER.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            log.error("Unable to read object to json string", e);
            return Optional.empty();
        }
    }

    /**
     * Read the given reader to a JsonNode
     *
     * @param reader the reader to read
     * @return JsonNode from reader or MissingNode if empty
     * @throws IOException if unable to read reader
     */
    public static JsonNode readTree(Reader reader) throws IOException {
        JsonNode jsonNode = OBJECT_MAPPER.readTree(reader);
        //Need to check if is MissingNode as v2.10.x of Jackson returns MissingNode if empty, earlier version return null
        return Optional.ofNullable(jsonNode).orElse(MissingNode.getInstance());
    }

}
