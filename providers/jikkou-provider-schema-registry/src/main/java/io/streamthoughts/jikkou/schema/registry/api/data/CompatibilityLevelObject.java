/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import org.jetbrains.annotations.NotNull;

/**
 * CompatibilityLevelObject.
 *
 * @param compatibilityLevel a compatibility level string.
 */
@Reflectable
public record CompatibilityLevelObject(@NotNull String compatibilityLevel) {

    /**
     * Creates a new {@link CompatibilityLevelObject} instance.
     *
     * @param compatibilityLevel a compatibility level string.
     */
    @ConstructorProperties({
            "compatibilityLevel",
    })
    public CompatibilityLevelObject {}

    /**
     * Gets the compatibility level.
     *
     * @return a compatibility level string.
     */
    @Override
    @JsonProperty("compatibilityLevel")
    public String compatibilityLevel() {
        return compatibilityLevel;
    }

}
