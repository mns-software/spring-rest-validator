package com.mnssoftware.validator.swagger.service.swagger;

import com.networknt.schema.ValidationMessage;
import io.swagger.models.Swagger;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * This class provides helpful methods for Swagger documentation
 *
 * @author msilcox
 */
public class SwaggerHelper {

    private static final MessageFormat FORMAT = new MessageFormat("{0}:validation failed");

    private SwaggerHelper() {
        //private constructor to hide default public one
    }

    public static Optional<NormalisedPath> findMatchingApiPath(final Swagger swagger, final NormalisedPath requestPath) {
        if (swagger != null) {
            return swagger.getPaths().keySet()
                    .stream()
                    .map(p -> (NormalisedPath) new ApiNormalisedPath(swagger, p))
                    .filter(p -> pathMatches(requestPath, p))
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

    public static ValidationMessage buildValidationMessage(String code, String name) {
        return new ValidationMessage.Builder().code(code).format(FORMAT).arguments(name).path(name).build();
    }

    private static boolean pathMatches(final NormalisedPath requestPath, final NormalisedPath apiPath) {
        if (requestPath.parts().size() != apiPath.parts().size()) {
            return false;
        }
        for (int i = 0; i < requestPath.parts().size(); i++) {
            if (requestPath.part(i).equalsIgnoreCase(apiPath.part(i)) || apiPath.isParam(i)) {
                continue;
            }
            return false;
        }
        return true;
    }

}
