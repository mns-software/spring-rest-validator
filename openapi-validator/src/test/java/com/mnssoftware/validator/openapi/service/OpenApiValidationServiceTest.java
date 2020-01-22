package com.mnssoftware.validator.openapi.service;

import com.mnssoftware.validator.openapi.service.openapi.OpenApiValidationService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.servlet.ServletException;
import java.nio.charset.StandardCharsets;

public class OpenApiValidationServiceTest {
    private static final String SWAGGER_PATH = "/petstore-simple.json";
    private static final String EMPTY_JSON_REQUEST = "";
    private static final String INVALID_JSON_REQUEST = "{\"tag\":\"lizard\",\"details\":{\"code\":true,\"message\":\"a message\"}}";
    private static final String VALID_JSON_REQUEST = "{\"name\":\"tiddles\",\"tag\":\"cat\",\"details\":{\"code\":123,\"message\":\"a message\"}}";

    private OpenApiValidationService underTest;

    private MockHttpServletRequest mockRequest = new MockHttpServletRequest();

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() {
        underTest = new OpenApiValidationService(SWAGGER_PATH);
        mockRequest.setMethod("POST");
        mockRequest.setRequestURI("/api/pets");
    }

    @Test
    public void validateRequest_emptyRequest_fails() throws ServletException {
        expected.expect(new ValidationExceptionMatcher("Request body is expected but not found"));
        mockRequest.setContent(EMPTY_JSON_REQUEST.getBytes(StandardCharsets.UTF_8));

        underTest.validateRequest(mockRequest);
    }

    @Test
    public void validateRequest_validJsonRequestWithErrors_fails() throws ServletException {
        expected.expect(new ValidationExceptionMatcher("The field name is mandatory", "The field tag does not have the correct enum value",
                "The field details.code does not have the correct type"));
        mockRequest.setContent(INVALID_JSON_REQUEST.getBytes(StandardCharsets.UTF_8));

        underTest.validateRequest(mockRequest);
    }

    @Test
    public void validateRequest_validJsonRequest_successful() throws ServletException {
        mockRequest.setContent(VALID_JSON_REQUEST.getBytes(StandardCharsets.UTF_8));
        underTest.validateRequest(mockRequest);
    }

    @Test
    public void validateRequest_unrecognisedPath_ignored() throws ServletException {
        mockRequest.setRequestURI("/api/children");

        underTest.validateRequest(mockRequest);
    }

    @Test
    public void validateRequest_unrecognisedMethod_throws() throws ServletException {
        expected.expect(HttpRequestMethodNotSupportedException.class);

        mockRequest.setMethod("HEAD");

        underTest.validateRequest(mockRequest);
    }

    @Test
    public void validateRequest_nonFieldError_fail() throws ServletException {
        expected.expect(
                new ValidationExceptionMatcher("The path parameter id does not have the correct type"));

        mockRequest.setMethod("GET");
        mockRequest.setRequestURI("/api/pets/fido");

        underTest.validateRequest(mockRequest);
    }

}