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
