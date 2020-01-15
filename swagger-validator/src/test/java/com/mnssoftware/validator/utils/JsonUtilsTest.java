package com.mnssoftware.validator.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import lombok.Data;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class JsonUtilsTest {

    @Test
    public void toString_valid_successful() {
        Optional<String> jsonString = JsonUtils.toString(new TestObj("stringVal", 123));

        assertThat(jsonString.isPresent(), equalTo(true));
        assertThat(jsonString.get(), equalTo("{\"prop1\":\"stringVal\",\"prop2\":123}"));
    }

    @Test
    public void toString_invalid_empty() {
        Optional<String> jsonString = JsonUtils.toString(new BadObj());

        assertThat(jsonString.isPresent(), equalTo(false));
    }

    @Test
    public void readTree_withReader_successful() throws IOException {
        JsonNode jsonNode = JsonUtils.readTree(new StringReader("{\"prop1\":\"stringVal\",\"prop2\":123}"));

        assertThat(jsonNode.get("prop1").asText(), equalTo("stringVal"));
        assertThat(jsonNode.get("prop2").asInt(), equalTo(123));
    }

    @Test
    public void readTree_withMissingNode_successful() throws IOException {
        JsonNode jsonNode = JsonUtils.readTree(new StringReader(""));

        assertThat(jsonNode, instanceOf(MissingNode.class));
    }

    @Data
    class TestObj {
        final String prop1;
        final int prop2;
    }

    class BadObj {
        String someVal = "dodgy";
    }
}