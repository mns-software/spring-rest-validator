package com.mnssoftware.validator.swagger.config;

import com.mnssoftware.validator.swagger.filter.ValidationFilter;
import com.mnssoftware.validator.swagger.interceptor.ValidationInterceptor;
import com.mnssoftware.validator.swagger.service.SwaggerValidationService;
import com.mnssoftware.validator.swagger.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for Request Validation
 * <p>
 * Conditional on property com.mnssoftware.validator=swagger
 */
@Configuration
@EnableConfigurationProperties(ValidationProperties.class)
@ConditionalOnProperty(prefix = "com.mnssoftware", name = "validator", havingValue = "swagger")
@Slf4j
public class ValidationConfig implements WebMvcConfigurer {

    private final ValidationProperties props;

    @Autowired
    public ValidationConfig(ValidationProperties props) {
        this.props = props;
        log.info("Validation enabled: {}", props.getSchemaLocation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        ValidationService service = new SwaggerValidationService(props.getSchemaLocation());
        registry.addInterceptor(new ValidationInterceptor(service)).excludePathPatterns(props.getExcludePathPatterns()).order(
                Ordered.LOWEST_PRECEDENCE);
    }

    @Bean
    public FilterRegistrationBean<ValidationFilter> validationFilterFilterRegistrationBean() {
        FilterRegistrationBean<ValidationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setName("validationFilter");
        registrationBean.setFilter(new ValidationFilter());
        registrationBean.setOrder(props.getFilterOrder());
        return registrationBean;
    }
}
