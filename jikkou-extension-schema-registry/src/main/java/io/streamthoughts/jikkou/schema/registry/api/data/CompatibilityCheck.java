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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.annotation.Reflectable;
import java.util.List;
import lombok.Getter;

@Reflectable
public final class CompatibilityCheck {

    private final boolean isCompatible;

    @Getter
    private final List<String> messages;


    /**
     * Creates a new {@link CompatibilityObject} instance.
     *
     * @param isCompatible {@code true}, if compatible. {@code false} otherwise.
     */
    @JsonCreator
    public CompatibilityCheck(@JsonProperty("is_compatible") boolean isCompatible,
                              @JsonProperty("messages") List<String> messages) {
        this.isCompatible = isCompatible;
        this.messages = messages;
    }

    /**
     * Gets the compatibility check test.
     *
     * @return  a compatibility level string.
     */
    @JsonProperty("is_compatible")
    public boolean isCompatible() {
        return isCompatible;
    }

    /**
     * Gets the error message.
     *
     * @return  the error message.
     */
    @JsonProperty("messages")
    public List<String> messages() {
        return this.messages;
    }
}
