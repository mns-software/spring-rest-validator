package com.mnssoftware.validator.service.swagger;

import io.swagger.models.Swagger;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * This utility normalize the RESTful API path so that they can be
 * handled identically.
 *
 * @author msilcox
 */
@Slf4j
public class ApiNormalisedPath implements NormalisedPath {
  private final List<String> pathParts;
  private final String original;
  private final String normalised;

  public ApiNormalisedPath(final Swagger swagger, final String path) {
    log.debug("path = {}", path);
    this.original = requireNonNull(path, "A path is required");
    this.normalised = normalise(swagger, path);
    log.debug("normalised = {}", this.normalised);
    this.pathParts = unmodifiableList(asList(normalised.split("/")));
  }

  @Override
  public List<String> parts() {
    return pathParts;
  }

  @Override
  public String part(int index) {
    if (index > -1 && index < pathParts.size()) {
      return pathParts.get(index);
    } else {
      return null;
    }
  }

  @Override
  public boolean isParam(int index) {
    final String part = part(index);
    return part.startsWith("{") && part.endsWith("}");
  }

  @Override
  public String paramName(int index) {
    if (!isParam(index)) {
      return null;
    }
    final String part = part(index);
    return part.substring(1, part.length() - 1);
  }

  @Override
  public String original() {
    return original;
  }

  @Override
  public String normalised() {
    return normalised;
  }

  private String normalise(Swagger swagger, String requestPath) {
    if (swagger != null && swagger.getBasePath() != null) {
      requestPath = requestPath.replaceFirst(swagger.getBasePath(), "");
    }
    if (!requestPath.startsWith("/")) {
      return "/" + requestPath;
    }
    return requestPath;
  }
}
