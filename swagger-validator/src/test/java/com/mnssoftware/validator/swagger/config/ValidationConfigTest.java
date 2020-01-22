package com.mnssoftware.validator.swagger.config;

import com.mnssoftware.validator.swagger.filter.ValidationFilter;
import com.mnssoftware.validator.swagger.interceptor.ValidationInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationConfigTest {

    @Mock
    private ValidationProperties props;

    @Mock
    private InterceptorRegistry registry;

    @Mock
    private InterceptorRegistration value;

    @InjectMocks
    private ValidationConfig underTest;

    @Before
    public void setUp() {
        when(props.getFilterOrder()).thenReturn(1000);
        when(props.getSchemaLocation()).thenReturn("petstore-simple.json");
        when(props.getExcludePathPatterns()).thenReturn(new String[]{"/example/**", "**/other"});
    }

    @Test
    public void addInterceptors_adds_successfully() {
        when(registry.addInterceptor(any(ValidationInterceptor.class))).thenReturn(value);
        when(value.excludePathPatterns(new String[]{"/example/**", "**/other"})).thenReturn(value);
        when(value.order(Integer.MAX_VALUE)).thenReturn(value);

        underTest.addInterceptors(registry);

        verify(registry).addInterceptor(any(ValidationInterceptor.class));
    }

    @Test
    public void validationFilterFilterRegistrationBean_registers_successfully() {

        FilterRegistrationBean<ValidationFilter> registrationBean = underTest.validationFilterFilterRegistrationBean();

        assertThat(registrationBean.getFilter(), instanceOf(ValidationFilter.class));
        assertThat(registrationBean.getOrder(), equalTo(1000));
    }

}