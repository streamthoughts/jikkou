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
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.List;
import lombok.Getter;

/**
 * Represents a response for a compatibility check.
 *
 * @param isCompatible {@code true}, if compatible. {@code false} otherwise.
 * @param messages      the error messages if not compatible.
 */
@Reflectable
public record CompatibilityCheck(boolean isCompatible, @Getter List<String> messages) {

    /**
     * Creates a new {@link CompatibilityObject} instance.
     */
    @ConstructorProperties({
            "is_compatible",
            "messages"
    })
    public CompatibilityCheck {}

    /**
     * Gets the compatibility check test.
     *
     * @return a compatibility level string.
     */
    @Override
    @JsonProperty("is_compatible")
    public boolean isCompatible() {
        return isCompatible;
    }

    /**
     * Gets the error message.
     *
     * @return the error message.
     */
    @Override
    @JsonProperty("messages")
    public List<String> messages() {
        return this.messages;
    }
}
