package com.mnssoftware.validator.swagger.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mnssoftware.validator.core.service.ValidationExceptionMatcher;
import com.mnssoftware.validator.swagger.service.swagger.NormalisedPath;
import com.mnssoftware.validator.swagger.service.swagger.SwaggerHelper;
import com.mnssoftware.validator.swagger.service.swagger.SwaggerOperation;
import com.networknt.schema.ValidationMessage;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static com.mnssoftware.validator.swagger.service.ValidatorTestUtil.stringParam;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestValidatorTest {

    private static final byte[] EMPTY_REQUEST = new byte[0];
    private static final String INVALID_JSON_REQUEST = "{\"tag\":\"lizard\",\"details\":{\"code\":true,\"message\":\"a message\"}}";
    private static final String VALID_JSON_REQUEST = "{\"name\":\"tiddles\",\"tag\":\"cat\",\"details\":{\"code\":123,\"message\":\"a message\"}}";

    @Mock
    private SchemaValidator schemaValidator;
    @Mock
    private NormalisedPath requestPath;
    @Mock
    private NormalisedPath apiPath;
    @Mock
    private SwaggerOperation swaggerOperation;
    @Mock
    private Operation operation;

    private MockHttpServletRequest request;
    private RequestValidator underTest;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() {
        underTest = new RequestValidator(schemaValidator);
        request = new MockHttpServletRequest();
        // defaulting to empty request body
        request.setContent(EMPTY_REQUEST);

        when(swaggerOperation.getPathString()).thenReturn(apiPath);
        when(swaggerOperation.getOperation()).thenReturn(operation);
        when(apiPath.parts()).thenReturn(asList("", "api", "pets"));
        when(apiPath.isParam(anyInt())).thenReturn(false);
    }

    @Test
    public void validateRequest_bodyHappyPath_successful() {
        Parameter parameter = mock(BodyParameter.class);
        when(operation.getParameters()).thenReturn(singletonList(parameter));
        when(parameter.getIn()).thenReturn("body");
        request.setContent(VALID_JSON_REQUEST.getBytes(UTF_8));

        Pair<String, Set<ValidationMessage>> validationMessages = underTest.validateRequest(requestPath, request, swaggerOperation);

        assertThat(validationMessages.getKey(), nullValue());
        assertThat(validationMessages.getValue(), empty());
    }

    @Test
    public void validateRequest_bodyInvalid_fails() {
        BodyParameter parameter = mock(BodyParameter.class);
        when(operation.getParameters()).thenReturn(singletonList(parameter));
        when(parameter.getIn()).thenReturn("body");
        when(parameter.getSchema()).thenReturn(mock(Model.class));
        request.setContent(INVALID_JSON_REQUEST.getBytes(UTF_8));
        when(schemaValidator.validate(any(JsonNode.class), any(Model.class))).thenReturn(
                Collections.singleton(SwaggerHelper.buildValidationMessage("1234", "$.aField")));

        Pair<String, Set<ValidationMessage>> validationMessages = underTest.validateRequest(requestPath, request, swaggerOperation);

        assertThat(validationMessages.getKey(), equalTo("field"));
        assertThat(validationMessages.getValue(), not(empty()));
        assertThat(validationMessages.getValue().iterator().next().getPath(), equalTo("$.aField"));
    }

    @Test
    public void validateRequest_unexpectedBody_fails() {
        request.setContent(VALID_JSON_REQUEST.getBytes(UTF_8));

        Pair<String, Set<ValidationMessage>> validationMessages = underTest.validateRequest(requestPath, request, swaggerOperation);

        assertThat(validationMessages.getKey(), equalTo("field"));
        assertThat(validationMessages.getValue(), not(empty()));
        ValidationMessage message = validationMessages.getValue().iterator().next();
        assertThat(message.getCode(), equalTo("9001"));
        assertThat(message.getPath(), equalTo("body"));
    }

    @Test
    public void validateRequest_emptyBodyNotExpected_successful() {

        Pair<String, Set<ValidationMessage>> validationMessages = underTest.validateRequest(requestPath, request, swaggerOperation);

        assertThat(validationMessages.getKey(), nullValue());
        assertThat(validationMessages.getValue(), empty());
    }

    @Test
    public void validateRequest_bodyMissingButRequired_fails() {
        BodyParameter parameter = mock(BodyParameter.class);
        when(parameter.getIn()).thenReturn("body");
        when(parameter.getRequired()).thenReturn(true);
        when(operation.getParameters()).thenReturn(singletonList(parameter));

        Pair<String, Set<ValidationMessage>> validationMessages = underTest.validateRequest(requestPath, request, swaggerOperation);

        assertThat(validationMessages.getKey(), equalTo("field"));
        assertThat(validationMessages.getValue(), not(empty()));
        ValidationMessage message = validationMessages.getValue().iterator().next();
        assertThat(message.getCode(), equalTo("9002"));
        assertThat(message.getPath(), equalTo("body"));
    }

    @Test
    public void validateRequest_bodyMissingNotRequired_successful() {
        BodyParameter parameter = mock(BodyParameter.class);
        when(parameter.getIn()).thenReturn("body");
        when(parameter.getRequired()).thenReturn(false);
        when(operation.getParameters()).thenReturn(singletonList(parameter));

        Pair<String, Set<ValidationMessage>> validationMessages = underTest.validateRequest(requestPath, request, swaggerOperation);

        assertThat(validationMessages.getKey(), nullValue());
        assertThat(validationMessages.getValue(), empty());
    }

    @Test
    public void validateRequest_dodgyReader_throws() throws IOException {
        expected.expect(new ValidationExceptionMatcher("The payload could not be parsed"));

        BodyParameter parameter = mock(BodyParameter.class);
        when(parameter.getIn()).thenReturn("body");
        when(operation.getParameters()).thenReturn(singletonList(parameter));
        HttpServletRequest badRequest = mock(HttpServletRequest.class);
        when(badRequest.getReader()).thenThrow(new IOException());

        Pair<String, Set<ValidationMessage>> validationMessages = underTest.validateRequest(requestPath, badRequest, swaggerOperation);

        assertThat(validationMessages.getKey(), nullValue());
        assertThat(validationMessages.getValue(), empty());
    }

    @Test
    public void validateRequest_pathParams_happyPath() {
        SerializableParameter parameter = stringParam(true, "path");
        when(operation.getParameters()).thenReturn(singletonList(parameter));
        when(apiPath.parts()).thenReturn(asList("", "api", "pets", "{type}"));
        when(apiPath.paramName(3)).thenReturn("type");
        when(apiPath.isParam(anyInt())).thenReturn(false, false, false, true);
        when(requestPath.part(3)).thenReturn("tigers");

        Pair<String, Set<ValidationMessage>> validationMessages = underTest.validateRequest(requestPath, request, swaggerOperation);

        assertThat(validationMessages.getKey(), nullValue());
        assertThat(validationMessages.getValue(), empty());
    }

    @Test
    public void validateRequest_pathParamsBadValue_fails() {
        SerializableParameter parameter = stringParam(true, "path");
        when(operation.getParameters()).thenReturn(singletonList(parameter));
        when(apiPath.parts()).thenReturn(asList("", "api", "pets", "{Test Parameter}"));
        when(apiPath.paramName(3)).thenReturn("Test Parameter");
        when(apiPath.isParam(anyInt())).thenReturn(false, false, false, true);

        Pair<String, Set<ValidationMessage>> validationMessages = underTest.validateRequest(requestPath, request, swaggerOperation);

        assertThat(validationMessages.getKey(), equalTo("path parameter"));
        assertThat(validationMessages.getValue(), not(empty()));
        assertThat(validationMessages.getValue().iterator().next().getPath(), equalTo("Test Parameter"));
    }

    @Test
    public void validateRequest_queryParams_happyPath() {
        SerializableParameter parameter = stringParam(true, "query");
        when(parameter.getName()).thenReturn("tags");
        when(operation.getParameters()).thenReturn(singletonList(parameter));
        request.setQueryString(UriUtils.encodeQuery("tags=tigers", UTF_8));

        Pair<String, Set<ValidationMessage>> validationMessages = underTest.validateRequest(requestPath, request, swaggerOperation);

        assertThat(validationMessages.getKey(), nullValue());
        assertThat(validationMessages.getValue(), empty());
    }

    @Test
    public void validateRequest_queryParamMissingNotRequired_happyPath() {
        SerializableParameter parameter = stringParam(true, "query");
        when(parameter.getName()).thenReturn("tags");
        when(parameter.getRequired()).thenReturn(false);
        when(operation.getParameters()).thenReturn(singletonList(parameter));

        Pair<String, Set<ValidationMessage>> validationMessages = underTest.validateRequest(requestPath, request, swaggerOperation);

        assertThat(validationMessages.getKey(), nullValue());
        assertThat(validationMessages.getValue(), empty());
    }

    @Test
    public void validateRequest_queryParamsBadValue_fails() {
        SerializableParameter parameter = stringParam(true, "query");
        when(parameter.getName()).thenReturn("tags");
        when(operation.getParameters()).thenReturn(singletonList(parameter));

        Pair<String, Set<ValidationMessage>> validationMessages = underTest.validateRequest(requestPath, request, swaggerOperation);

        assertThat(validationMessages.getKey(), equalTo("query parameter"));
        assertThat(validationMessages.getValue(), not(empty()));
        assertThat(validationMessages.getValue().iterator().next().getPath(), equalTo("tags"));
    }
}
