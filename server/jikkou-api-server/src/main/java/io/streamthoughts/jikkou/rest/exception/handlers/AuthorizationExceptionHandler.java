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
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.security.authentication.AuthorizationException;
import io.streamthoughts.jikkou.rest.data.ErrorEntity;
import io.streamthoughts.jikkou.rest.data.ErrorResponse;
import io.streamthoughts.jikkou.rest.data.errors.Errors;
import jakarta.inject.Singleton;
import java.util.List;

@Produces
@Singleton
@Requires(classes = {AuthorizationException.class, ExceptionHandler.class})
@Primary
public class AuthorizationExceptionHandler implements ExceptionHandler<AuthorizationException, HttpResponse<?>> {

    /**
     * {{@inheritDoc}
     **/
    @Override
    public HttpResponse<?> handle(HttpRequest request, AuthorizationException exception) {
        ErrorEntity error = new ErrorEntity(
                HttpStatus.UNAUTHORIZED.getCode(),
                Errors.AUTHENTICATION_USER_UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED.getReason()
        );
        return HttpResponse.unauthorized().body(new ErrorResponse("Unauthorized", List.of(error)));
    }
}
