package com.mnssoftware.validator.swagger.service.swagger;

import com.mnssoftware.validator.core.service.NormalisedPath;
import io.swagger.models.Operation;
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
    private final Operation operation;
}
