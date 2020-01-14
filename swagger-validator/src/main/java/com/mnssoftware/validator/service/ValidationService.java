package com.mnssoftware.validator.service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Interface for Validation Services
 */
public interface ValidationService {

  /**
   * <p>Validate the given request.</p>
   * <p>Note the request should be a {@code MultiReadHttpServletRequest} to ensure the body can be read by this service.<br/>
   * If the request is not a {@code MultiReadHttpServletRequest} then validation will be disabled</p>
   *
   * <p>Throws ValidationException if validation fails</p>
   *
   * @param request request to validate
   */
  void validateRequest(HttpServletRequest request) throws ServletException;
}
