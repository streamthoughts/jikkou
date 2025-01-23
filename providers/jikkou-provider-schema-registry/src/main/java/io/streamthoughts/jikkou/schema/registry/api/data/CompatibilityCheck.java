/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.List;

/**
 * Represents a response for a compatibility check.
 *
 * @param isCompatible {@code true}, if compatible. {@code false} otherwise.
 * @param messages     the error messages if not compatible.
 */
@Reflectable
public record CompatibilityCheck(@JsonProperty("is_compatible") boolean isCompatible,
                                 @JsonProperty("messages") List<String> messages) {

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
