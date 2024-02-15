/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.exception.handlers;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.streamthoughts.jikkou.rest.data.ErrorEntity;
import io.streamthoughts.jikkou.rest.data.ErrorResponse;
import io.streamthoughts.jikkou.rest.data.errors.Errors;
import jakarta.inject.Singleton;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Primary
@Produces
@Singleton
@Requires(classes = {Exception.class, DefaultHttpExceptionHandler.class})
public final class DefaultHttpExceptionHandler implements ExceptionHandler<Exception, HttpResponse<?>> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpExceptionHandler.class);

    /**
     * {{@inheritDoc}
     **/
    @Override
    @Error(global = true, exception = Exception.class)
    public HttpResponse<?> handle(HttpRequest request, Exception exception) {
        LOG.error("Internal Server Error", exception);
        ErrorResponse response = new ErrorResponse(
                "Internal Server Error",
                List.of(new ErrorEntity(
                                HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                                Errors.INTERNAL_SERVER_ERROR_CODE,
                                exception.getMessage()
                        )
                )
        );
        return HttpResponse.serverError(response);
    }
}
