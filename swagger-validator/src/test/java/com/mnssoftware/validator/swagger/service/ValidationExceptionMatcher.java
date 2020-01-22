package com.mnssoftware.validator.swagger.service;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

class ValidationExceptionMatcher extends BaseMatcher<ValidationException> {
    private final String[] messages;

    public ValidationExceptionMatcher(String... messages) {
        this.messages = messages;
    }

    @Override
    public boolean matches(Object item) {
        assertNotNull(item);
        List<String> validationErrors = ((ValidationException) item).getValidationErrors();
        assertNotNull(validationErrors);
        assertThat(validationErrors.size(), equalTo(messages.length));
        assertThat(validationErrors, containsInAnyOrder(messages));
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(ValidationException.class.getSimpleName()).appendText(" with error messages [").appendText(
                StringUtils.join(messages)).appendText("]");
    }
}
