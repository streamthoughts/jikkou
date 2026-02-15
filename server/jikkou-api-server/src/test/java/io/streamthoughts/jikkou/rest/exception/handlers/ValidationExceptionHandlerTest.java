/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.exception.handlers;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.rest.data.ErrorEntity;
import io.streamthoughts.jikkou.rest.data.ErrorResponse;
import io.streamthoughts.jikkou.rest.data.errors.Errors;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ValidationExceptionHandlerTest {

    private final ValidationExceptionHandler handler = new ValidationExceptionHandler();

    @SuppressWarnings("unchecked")
    private final HttpRequest<?> mockRequest = Mockito.mock(HttpRequest.class);

    @Test
    void shouldReturnServerErrorWhenValidationExceptionHandled() {
        // Given
        ValidationException exception = new ValidationException(
                List.of(new ValidationError("constraint", "error message")));

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void shouldReturnCorrectMessageWhenValidationFails() {
        // Given
        ValidationException exception = new ValidationException(
                List.of(new ValidationError("constraint", "error message")));

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("Resource Validation Failed", body.message());
    }

    @Test
    void shouldMapValidationErrorToErrorEntityWhenOneErrorPresent() {
        // Given
        ValidationException exception = new ValidationException(
                List.of(new ValidationError("constraint", "validation failed")));

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(1, body.errors().size());
        ErrorEntity error = body.errors().get(0);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.getCode(), error.status());
        Assertions.assertEquals(Errors.API_RESOURCE_VALIDATION_FAILED_ERROR_CODE, error.errorCode());
        Assertions.assertEquals("validation failed", error.message());
    }

    @Test
    void shouldMapMultipleErrorsWhenMultipleValidationErrorsPresent() {
        // Given
        ValidationException exception = new ValidationException(List.of(
                new ValidationError("constraint1", "error one"),
                new ValidationError("constraint2", "error two"),
                new ValidationError("constraint3", "error three")));

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(3, body.errors().size());
    }

    @Test
    void shouldIncludeNameAndDetailsWhenValidationErrorHasDetails() {
        // Given
        Map<String, Object> details = Map.of("key", "value");
        ValidationException exception = new ValidationException(
                List.of(new ValidationError("constraintName", null, "error message", details)));

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        ErrorEntity error = body.errors().get(0);
        Assertions.assertNotNull(error.details());
        Assertions.assertEquals("constraintName", error.details().get("name"));
        Assertions.assertEquals(details, error.details().get("details"));
    }

    // BUG: The HTTP response status is 500 (serverError) but ErrorEntity status is 400 (BAD_REQUEST).
    // Validation failures are client errors and should return HTTP 400, not 500.
    @Test
    void shouldReturnInconsistentStatusCodes() {
        // Given
        ValidationException exception = new ValidationException(
                List.of(new ValidationError("constraint", "validation failed")));

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        // HTTP response is 500 (server error)
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        // But error entity says 400 (client error) — these are inconsistent
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.getCode(), body.errors().get(0).status());
    }

    @Test
    void shouldHandleEmptyErrorListWhenNoValidationErrors() {
        // Given
        ValidationException exception = new ValidationException(Collections.emptyList());

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("Resource Validation Failed", body.message());
        Assertions.assertTrue(body.errors().isEmpty());
    }
}
