package com.mnssoftware.validator.openapi.filter;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MultiReadHttpServletRequestTest {

    private MockHttpServletRequest mockRequest = new MockHttpServletRequest();

    @Test
    public void constructor_errorReadingContent_defaultsEmpty() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenThrow(new IOException());

        MultiReadHttpServletRequest underTest = new MultiReadHttpServletRequest(request);

        assertThat(underTest.getInputStream(), instanceOf(MultiReadHttpServletRequest.DelegatingServletInputStream.class));
        assertThat(readStream(underTest.getInputStream()), equalTo(""));
    }

    @Test
    public void getInputStream_withRequest_multiRead() throws IOException {
        mockRequest.setContent("I am the content".getBytes(StandardCharsets.UTF_8));
        MultiReadHttpServletRequest underTest = new MultiReadHttpServletRequest(mockRequest);

        assertThat(underTest.getInputStream(), instanceOf(MultiReadHttpServletRequest.DelegatingServletInputStream.class));

        assertThat(readStream(underTest.getInputStream()), equalTo("I am the content"));
        assertThat(readStream(underTest.getInputStream()), equalTo("I am the content"));
    }

    @Test
    public void getReader_withRequest_multiRead() throws IOException {
        mockRequest.setContent("I am the content".getBytes(StandardCharsets.UTF_8));
        MultiReadHttpServletRequest underTest = new MultiReadHttpServletRequest(mockRequest);

        assertThat(readReader(underTest.getReader()), equalTo("I am the content"));
        assertThat(readReader(underTest.getReader()), equalTo("I am the content"));
    }

    private String readStream(ServletInputStream inputStream) throws IOException {
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }

    private String readReader(BufferedReader reader) throws IOException {
        return IOUtils.toString(reader);
    }

    // DelegatingServletInputStream tests

    @Test(expected = UnsupportedOperationException.class)
    public void delegatingServletInputStream_setReadListener_throws() throws IOException {
        try (MultiReadHttpServletRequest.DelegatingServletInputStream underTest = new MultiReadHttpServletRequest.DelegatingServletInputStream(
                mock(InputStream.class))) {

            underTest.setReadListener(null);
        }
    }

    @Test
    public void delegatingServletInputStream_reading_successful() throws IOException {
        mockRequest.setContent("I am the content".getBytes(StandardCharsets.UTF_8));
        MultiReadHttpServletRequest.DelegatingServletInputStream underTest = new MultiReadHttpServletRequest.DelegatingServletInputStream(
                mockRequest.getInputStream());

        assertThat(underTest.isReady(), equalTo(true));
        assertThat(underTest.isFinished(), equalTo(false));
        assertThat(underTest.available(), equalTo(16));
        assertThat(IOUtils.toString(underTest, StandardCharsets.UTF_8), equalTo("I am the content"));
        assertThat(underTest.isFinished(), equalTo(true));
        underTest.close();
        assertThat(underTest.available(), equalTo(0));
    }
}