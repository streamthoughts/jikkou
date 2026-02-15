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
import io.streamthoughts.jikkou.rest.data.ErrorEntity;
import io.streamthoughts.jikkou.rest.data.ErrorResponse;
import io.streamthoughts.jikkou.rest.data.errors.Errors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DefaultHttpExceptionHandlerTest {

    private final DefaultHttpExceptionHandler handler = new DefaultHttpExceptionHandler();

    @SuppressWarnings("unchecked")
    private final HttpRequest<?> mockRequest = Mockito.mock(HttpRequest.class);

    @Test
    void shouldReturnServerErrorWhenExceptionHandled() {
        // Given
        Exception exception = new RuntimeException("test error");

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void shouldReturnErrorResponseWithMessageWhenExceptionHandled() {
        // Given
        Exception exception = new RuntimeException("test error");

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("Internal Server Error", body.message());
    }

    @Test
    void shouldReturnErrorEntityWithCorrectCodeWhenExceptionHandled() {
        // Given
        Exception exception = new RuntimeException("test error");

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(1, body.errors().size());
        ErrorEntity error = body.errors().get(0);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), error.status());
        Assertions.assertEquals(Errors.INTERNAL_SERVER_ERROR_CODE, error.errorCode());
    }

    @Test
    void shouldIncludeExceptionMessageWhenExceptionHasMessage() {
        // Given
        Exception exception = new RuntimeException("specific error message");

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("specific error message", body.errors().get(0).message());
    }

    @Test
    void shouldHandleNullMessageWhenExceptionHasNoMessage() {
        // Given
        Exception exception = new RuntimeException();

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        Assertions.assertNull(body.errors().get(0).message());
    }
}
