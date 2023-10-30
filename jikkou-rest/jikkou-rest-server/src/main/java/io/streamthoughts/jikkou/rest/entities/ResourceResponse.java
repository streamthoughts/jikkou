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
