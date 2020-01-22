/*
 * Copyright (c) 2019 MNS Software Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mnssoftware.validator.openapi.service.openapi;

import com.networknt.oas.model.OpenApi3;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * This class provides helpful methods for OpenApi documentation
 *
 * @author msilcox
 */
@Slf4j
public class OpenApiHelper {
    private static final MessageFormat FORMAT = new MessageFormat("{0}:validation failed");

    public static Optional<NormalisedPath> findMatchingApiPath(final OpenApi3 openApi3, final NormalisedPath requestPath) {
        if (openApi3 != null) {
            return openApi3.getPaths().keySet()
                    .stream()
                    .map(p -> (NormalisedPath) new ApiNormalisedPath(p))
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
