package com.mnssoftware.validator.swagger.service.swagger;

import com.mnssoftware.validator.core.service.NormalisedPath;
import com.networknt.schema.ValidationMessage;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SwaggerHelperTest {

    @Mock
    private NormalisedPath normalisedPath;

    @Mock
    private Swagger swagger;

    @Mock
    private Path path;

    @Test
    public void buildValidationMessage_builds_successful() {
        ValidationMessage underTest = SwaggerHelper.buildValidationMessage("1234", "field1");

        assertThat(underTest.getCode(), equalTo("1234"));
        assertThat(underTest.getPath(), equalTo("field1"));
        assertThat(underTest.getArguments(), equalTo(new String[]{"field1"}));
    }

    @Test
    public void findMatchingApiPath_nullSwagger_returnsEmpty() {

        Optional<NormalisedPath> matchingApiPath = SwaggerHelper.findMatchingApiPath(null, normalisedPath);

        assertThat(matchingApiPath.isPresent(), equalTo(false));
    }

    @Test
    public void findMatchingApiPath_withPath_successful() {
        when(swagger.getPaths()).thenReturn(Collections.singletonMap("/api/foo", path));
        when(normalisedPath.parts()).thenReturn(Arrays.asList("", "api", "foo"));
        when(normalisedPath.part(anyInt())).thenReturn("", "api", "foo");

        Optional<NormalisedPath> matchingApiPath = SwaggerHelper.findMatchingApiPath(swagger, normalisedPath);

        assertThat(matchingApiPath.isPresent(), equalTo(true));
    }

    @Test
    public void findMatchingApiPath_withPathParam_successful() {
        when(swagger.getPaths()).thenReturn(Collections.singletonMap("/api/{bar}", path));
        when(normalisedPath.parts()).thenReturn(Arrays.asList("", "api", "foo"));
        when(normalisedPath.part(anyInt())).thenReturn("", "api", "foo");

        Optional<NormalisedPath> matchingApiPath = SwaggerHelper.findMatchingApiPath(swagger, normalisedPath);

        assertThat(matchingApiPath.isPresent(), equalTo(true));
    }

    @Test
    public void findMatchingApiPath_notMatches_unsuccessful() {
        when(swagger.getPaths()).thenReturn(Collections.singletonMap("/api/bar", path));
        when(normalisedPath.parts()).thenReturn(Arrays.asList("", "api", "foo"));
        when(normalisedPath.part(anyInt())).thenReturn("", "api", "foo");

        Optional<NormalisedPath> matchingApiPath = SwaggerHelper.findMatchingApiPath(swagger, normalisedPath);

        assertThat(matchingApiPath.isPresent(), equalTo(false));
    }

    @Test
    public void findMatchingApiPath_notMatchesDifferentLength_unsuccessful() {
        when(swagger.getPaths()).thenReturn(Collections.singletonMap("/api/bar/tab", path));
        when(normalisedPath.parts()).thenReturn(Arrays.asList("", "api", "foo"));

        Optional<NormalisedPath> matchingApiPath = SwaggerHelper.findMatchingApiPath(swagger, normalisedPath);

        assertThat(matchingApiPath.isPresent(), equalTo(false));
    }
}
