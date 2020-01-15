package com.mnssoftware.validator.filter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValidationFilterTest {

    @Mock
    private FilterChain filterChain;
    private MockHttpServletResponse response = new MockHttpServletResponse();
    private MockHttpServletRequest request = new MockHttpServletRequest();

    private ValidationFilter underTest = new ValidationFilter();

    @Test
    public void doFilterInternal_wrapsRequest_successful() throws Exception {
        underTest.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(isA(MultiReadHttpServletRequest.class), eq(response));
    }
}