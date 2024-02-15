/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.entities;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.micronaut.http.hateoas.AbstractResource;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public final class ResourceResponse<T> extends AbstractResource<ResourceResponse<T>> {

    private final T response;

    public ResourceResponse(@NotNull T response) {
        this.response = Objects.requireNonNull(response, "response must not be null");
    }

    @JsonUnwrapped
    public T response() {
        return response;
    }
}
