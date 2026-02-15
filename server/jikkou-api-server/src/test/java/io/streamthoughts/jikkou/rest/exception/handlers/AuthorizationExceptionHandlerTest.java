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
import io.micronaut.security.authentication.AuthorizationException;
import io.streamthoughts.jikkou.rest.data.ErrorEntity;
import io.streamthoughts.jikkou.rest.data.ErrorResponse;
import io.streamthoughts.jikkou.rest.data.errors.Errors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AuthorizationExceptionHandlerTest {

    private final AuthorizationExceptionHandler handler = new AuthorizationExceptionHandler();

    @SuppressWarnings("unchecked")
    private final HttpRequest<?> mockRequest = Mockito.mock(HttpRequest.class);

    @Test
    void shouldReturnUnauthorizedWhenHandled() {
        // Given
        AuthorizationException exception = new AuthorizationException(null);

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatus());
    }

    @Test
    void shouldReturnCorrectMessageWhenHandled() {
        // Given
        AuthorizationException exception = new AuthorizationException(null);

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("Unauthorized", body.message());
    }

    @Test
    void shouldReturnErrorEntityWithUnauthorizedCodeWhenHandled() {
        // Given
        AuthorizationException exception = new AuthorizationException(null);

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(1, body.errors().size());
        ErrorEntity error = body.errors().get(0);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.getCode(), error.status());
        Assertions.assertEquals(Errors.AUTHENTICATION_USER_UNAUTHORIZED, error.errorCode());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.getReason(), error.message());
    }
}
