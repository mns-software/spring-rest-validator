package com.mnssoftware.validator.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Validator Filter that wraps the request to allow multiple reads of the body for validation
 *
 * @author msilcox
 */
public class ValidationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    MultiReadHttpServletRequest wrappedRequest = new MultiReadHttpServletRequest(request);
    filterChain.doFilter(wrappedRequest, response);
  }

}
