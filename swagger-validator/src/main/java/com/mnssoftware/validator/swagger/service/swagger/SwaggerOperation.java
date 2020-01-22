package com.mnssoftware.validator.swagger.service.swagger;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import lombok.Data;

/**
 * A container representing a single API operation.
 * <p>
 * This includes the path, method and operation components from the OAI spec object. Used as a
 * convenience to hold related information in one place.
 *
 * @author msilcox
 */
@Data
public class SwaggerOperation {
    private final NormalisedPath pathString;
    private final Path pathObject;
    private final HttpMethod method;
    private final Operation operation;
}
