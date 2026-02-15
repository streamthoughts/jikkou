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
import io.streamthoughts.jikkou.rest.exception.ApiResourceNotFoundException;
import io.streamthoughts.jikkou.rest.models.ApiResourceIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ApiResourceNotFoundExceptionHandlerTest {

    private final ApiResourceNotFoundExceptionHandler handler = new ApiResourceNotFoundExceptionHandler();

    @SuppressWarnings("unchecked")
    private final HttpRequest<?> mockRequest = Mockito.mock(HttpRequest.class);

    @Test
    void shouldReturnNotFoundWhenHandled() {
        // Given
        ApiResourceIdentifier identifier = new ApiResourceIdentifier("kafka.jikkou.io", "v1beta2", "kafkatopics");
        ApiResourceNotFoundException exception = new ApiResourceNotFoundException(identifier);

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    void shouldReturnCorrectMessageWhenHandled() {
        // Given
        ApiResourceIdentifier identifier = new ApiResourceIdentifier("kafka.jikkou.io", "v1beta2", "kafkatopics");
        ApiResourceNotFoundException exception = new ApiResourceNotFoundException(identifier);

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("Resource Not Found", body.message());
    }

    @Test
    void shouldReturnErrorEntityWithNotFoundCodeWhenHandled() {
        // Given
        ApiResourceIdentifier identifier = new ApiResourceIdentifier("kafka.jikkou.io", "v1beta2", "kafkatopics");
        ApiResourceNotFoundException exception = new ApiResourceNotFoundException(identifier);

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(1, body.errors().size());
        ErrorEntity error = body.errors().get(0);
        Assertions.assertEquals(HttpStatus.NOT_FOUND.getCode(), error.status());
        Assertions.assertEquals(Errors.API_RESOURCE_TYPE_NOT_FOUND_ERROR_CODE, error.errorCode());
    }

    @Test
    void shouldIncludeIdentifierInErrorMessageWhenHandled() {
        // Given
        ApiResourceIdentifier identifier = new ApiResourceIdentifier("kafka.jikkou.io", "v1beta2", "kafkatopics");
        ApiResourceNotFoundException exception = new ApiResourceNotFoundException(identifier);

        // When
        HttpResponse<?> response = handler.handle(mockRequest, exception);

        // Then
        ErrorResponse body = (ErrorResponse) response.body();
        Assertions.assertNotNull(body);
        String errorMessage = body.errors().get(0).message();
        Assertions.assertTrue(errorMessage.contains("kafka.jikkou.io"));
        Assertions.assertTrue(errorMessage.contains("v1beta2"));
        Assertions.assertTrue(errorMessage.contains("kafkatopics"));
    }
}
