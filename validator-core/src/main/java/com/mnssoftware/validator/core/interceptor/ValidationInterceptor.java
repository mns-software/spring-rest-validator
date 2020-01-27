package com.mnssoftware.validator.core.interceptor;

import com.mnssoftware.validator.core.filter.MultiReadHttpServletRequest;
import com.mnssoftware.validator.core.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ValidationInterceptor extends HandlerInterceptorAdapter {

    private ValidationService validationService;

    public ValidationInterceptor(final ValidationService validationService) {
        this.validationService = requireNonNull(validationService, "validationService must not be null");
    }

    @Override
    public boolean preHandle(final HttpServletRequest servletRequest,
                             final HttpServletResponse servletResponse,
                             final Object handler) throws Exception {
        // only wrapped servlet requests can be validated - see: ValidationFilter
        if (!(servletRequest instanceof MultiReadHttpServletRequest)) {
            log.debug("Request validation disabled");
            return super.preHandle(servletRequest, servletResponse, handler);
        }

        validationService.validateRequest(servletRequest);

        return true;
    }
}
