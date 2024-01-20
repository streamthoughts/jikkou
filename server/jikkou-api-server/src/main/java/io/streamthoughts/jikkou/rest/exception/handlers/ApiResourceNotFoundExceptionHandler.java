/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.rest.exception.handlers;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.streamthoughts.jikkou.rest.data.ErrorEntity;
import io.streamthoughts.jikkou.rest.data.ErrorResponse;
import io.streamthoughts.jikkou.rest.data.errors.Errors;
import io.streamthoughts.jikkou.rest.exception.ApiResourceNotFoundException;
import jakarta.inject.Singleton;
import java.util.List;

@Produces
@Singleton
@Requires(classes = {ApiResourceNotFoundException.class, ExceptionHandler.class})
@Primary
public class ApiResourceNotFoundExceptionHandler implements ExceptionHandler<ApiResourceNotFoundException, HttpResponse<?>> {

    /** {@inheritDoc} **/
    @Override
    public HttpResponse<?> handle(HttpRequest request, ApiResourceNotFoundException exception) {
        final ErrorResponse error = new ErrorResponse(
                "Resource Not Found",
                List.of(new ErrorEntity(
                                HttpStatus.NOT_FOUND.getCode(),
                                Errors.API_RESOURCE_TYPE_NOT_FOUND_ERROR_CODE,
                                exception.getLocalizedMessage()
                        )
                )
        );
        return HttpResponse.notFound(error);
    }
}
