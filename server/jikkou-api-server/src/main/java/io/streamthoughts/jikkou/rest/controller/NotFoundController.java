/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.controller;


import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.Link;
import io.streamthoughts.jikkou.rest.data.ErrorEntity;
import io.streamthoughts.jikkou.rest.data.ErrorResponse;
import io.streamthoughts.jikkou.rest.data.errors.Errors;
import io.streamthoughts.jikkou.rest.entities.ResourceResponse;
import java.util.List;

@Controller
public class NotFoundController extends AbstractController {

    @Produces
    @Error(status = HttpStatus.NOT_FOUND, global = true)
    public HttpResponse<?> notFound(HttpRequest request) {
        ErrorResponse error = new ErrorResponse(
                "Not Found",
                List.of(new ErrorEntity(
                                HttpStatus.NOT_FOUND.getCode(),
                                Errors.NOT_FOUND
                        )
                )
        );
        ResourceResponse<ErrorResponse> response = new ResourceResponse<>(error)
                .link(Link.SELF, getSelfLink(request));
        return HttpResponse.notFound(response);
    }
}
