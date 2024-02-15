/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.exception.handlers;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.rest.data.ErrorEntity;
import io.streamthoughts.jikkou.rest.data.ErrorResponse;
import io.streamthoughts.jikkou.rest.data.errors.Errors;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

@Produces
@Singleton
@Requires(classes = {ValidationException.class, ExceptionHandler.class})
public class ValidationExceptionHandler implements ExceptionHandler<ValidationException, HttpResponse<?>> {

    /**
     * {{@inheritDoc}
     **/
    @Override
    public HttpResponse<?> handle(HttpRequest request, ValidationException exception) {
        ErrorResponse response = buildErrorResponseForValidationFailure(exception.errors());
        return HttpResponse.serverError(response);
    }

    @NotNull
    private ErrorResponse buildErrorResponseForValidationFailure(List<ValidationError> errors) {
        List<ErrorEntity> httpErrors = errors.stream()
                .map(error -> {
                            Map<String, Object> errorEntryDetails = new HashMap<>();
                            errorEntryDetails.put("name", error.name());
                            errorEntryDetails.put("details", error.details());
                            return new ErrorEntity(
                                    HttpStatus.BAD_REQUEST.getCode(),
                                    Errors.API_RESOURCE_VALIDATION_FAILED_ERROR_CODE,
                                    error.message(),
                                    errorEntryDetails
                            );
                        }
                )
                .toList();
        return new ErrorResponse("Resource Validation Failed", httpErrors);
    }
}
