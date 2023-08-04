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
package io.streamthoughts.jikkou.api.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.annotation.Reflectable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Reflectable
public record ChangeError(@NotNull String message, @Nullable Integer status) {

    public ChangeError(final String message) {
        this(message, null);
    }

    public ChangeError(@NotNull final String message,
                       @Nullable final Integer status) {
        this.message = Objects.requireNonNull(message, "message must not be null");
        this.status = status;
    }

    @Override
    @JsonProperty("message")
    public String message() {
        return message;
    }

    @Override
    @JsonProperty("status")
    public Integer status() {
        return status;
    }

    @Override
    public String toString() {
        return message;
    }
}