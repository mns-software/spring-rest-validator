package com.mnssoftware.validator.interceptor;

import com.mnssoftware.validator.filter.MultiReadHttpServletRequest;
import com.mnssoftware.validator.service.ValidationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ValidationInterceptorTest {

    @Mock
    private ValidationService validationService;

    @Test(expected = NullPointerException.class)
    public void constructor_nullService_throws() {
        new ValidationInterceptor(null);
    }

    @Test
    public void preHandle_wrappedRequest_handledAndValidated() throws Exception {
        ValidationInterceptor underTest = new ValidationInterceptor(validationService);

        MultiReadHttpServletRequest request = new MultiReadHttpServletRequest(new MockHttpServletRequest());
        boolean handled = underTest.preHandle(request, new MockHttpServletResponse(), null);

        assertThat(handled, equalTo(true));
        verify(validationService).validateRequest(request);
    }

    @Test
    public void preHandle_unWrappedRequest_handledAndNotValidated() throws Exception {
        ValidationInterceptor underTest = new ValidationInterceptor(validationService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        boolean handled = underTest.preHandle(request, new MockHttpServletResponse(), null);

        assertThat(handled, equalTo(true));
        verify(validationService, never()).validateRequest(request);
    }
}