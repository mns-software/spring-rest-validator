package com.mnssoftware.validator.interceptor;

import com.mnssoftware.validator.filter.MultiReadHttpServletRequest;
import com.mnssoftware.validator.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ValidationInterceptor extends HandlerInterceptorAdapter {

  private ValidationService validationService;

  public ValidationInterceptor(@Nonnull final ValidationService validationService) {
    requireNonNull(validationService, "validationService must not be null");
    this.validationService = validationService;
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
