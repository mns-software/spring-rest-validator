package com.mnssoftware.validator.swagger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.mnssoftware")
@Data
public class ValidationProperties {

    /**
     * The OpenAPI/Swagger JSON schema location to be used for validation
     */
    private String schemaLocation = "swagger.json";

    /**
     * Array of excluded path patterns for the interceptor
     */
    private String[] excludePathPatterns = new String[0];

    /**
     * Order value for the filter (default 0)
     */
    private int filterOrder = 0;

}
