/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.api.health;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.annotation.Reflectable;
import java.util.Objects;

@Reflectable
public record Status(String code) {

    public static final Status UNKNOWN = new Status("UNKNOWN");
    public static final Status UP = new Status("UP");
    public static final Status DOWN = new Status("DOWN");

    /**
     * Creates a new {@link Status} instance.
     *
     * @param code the status code.
     */
    public Status {
        Objects.requireNonNull(code, "code cannot be null");
    }

    @Override
    @JsonProperty("status")
    public String code() {
        return code;
    }

}