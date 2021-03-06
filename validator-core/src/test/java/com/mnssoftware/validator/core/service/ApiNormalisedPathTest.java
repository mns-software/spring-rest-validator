package com.mnssoftware.validator.core.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ApiNormalisedPathTest {

    @Test
    public void constructor_normalises_successful() {
        ApiNormalisedPath underTest = new ApiNormalisedPath("", "/my/api");

        assertThat(underTest.normalised(), equalTo("/my/api"));
        assertThat(underTest.original(), equalTo("/my/api"));
        assertThat(underTest.parts(), equalTo(Arrays.asList("", "my", "api")));
    }

    @Test
    public void constructor_nullSwagger_successful() {
        ApiNormalisedPath underTest = new ApiNormalisedPath(null, "/my/api");

        assertThat(underTest.normalised(), equalTo("/my/api"));
        assertThat(underTest.original(), equalTo("/my/api"));
        assertThat(underTest.parts(), equalTo(Arrays.asList("", "my", "api")));
    }

    @Test
    public void constructor_nullBasePath_successful() {
        ApiNormalisedPath underTest = new ApiNormalisedPath("", "/my/api");

        assertThat(underTest.normalised(), equalTo("/my/api"));
        assertThat(underTest.original(), equalTo("/my/api"));
        assertThat(underTest.parts(), equalTo(Arrays.asList("", "my", "api")));
    }

    @Test
    public void constructor_swaggerBasePath_successful() {

        ApiNormalisedPath underTest = new ApiNormalisedPath("/v2", "/v2/my/api");

        assertThat(underTest.normalised(), equalTo("/my/api"));
        assertThat(underTest.original(), equalTo("/v2/my/api"));
        assertThat(underTest.parts(), equalTo(Arrays.asList("", "my", "api")));
    }

    @Test
    public void constructor_withoutFirstSlash_successful() {

        ApiNormalisedPath underTest = new ApiNormalisedPath("", "my/api");

        assertThat(underTest.normalised(), equalTo("/my/api"));
        assertThat(underTest.original(), equalTo("my/api"));
        assertThat(underTest.parts(), equalTo(Arrays.asList("", "my", "api")));
    }

    @Test
    public void part_nullSwagger_successful() {
        ApiNormalisedPath underTest = new ApiNormalisedPath("", "/my/api");

        assertThat(underTest.part(0), equalTo(""));
        assertThat(underTest.part(1), equalTo("my"));
        assertThat(underTest.part(2), equalTo("api"));
    }

    @Test
    public void part_indexOutOfRange_successful() {
        ApiNormalisedPath underTest = new ApiNormalisedPath("", "/my/api");

        assertThat(underTest.part(4), nullValue());
        assertThat(underTest.part(-2), nullValue());
    }

    @Test
    public void isParam_withParam_successful() {
        ApiNormalisedPath underTest = new ApiNormalisedPath("", "/my}/{api/{somat}");

        assertThat(underTest.isParam(0), equalTo(false));
        assertThat(underTest.isParam(1), equalTo(false));
        assertThat(underTest.isParam(2), equalTo(false));
        assertThat(underTest.isParam(3), equalTo(true));
    }

    @Test
    public void paramName_withParam_successful() {
        ApiNormalisedPath underTest = new ApiNormalisedPath("", "/my/api/{somat}");

        assertThat(underTest.paramName(0), nullValue());
        assertThat(underTest.paramName(1), nullValue());
        assertThat(underTest.paramName(2), nullValue());
        assertThat(underTest.paramName(3), equalTo("somat"));
    }
}