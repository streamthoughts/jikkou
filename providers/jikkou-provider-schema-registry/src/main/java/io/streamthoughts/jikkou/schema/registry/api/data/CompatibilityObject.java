/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import org.jetbrains.annotations.NotNull;

/**
 * CompatibilityObject.
 *
 * @param compatibility a compatibility level string.
 */
@Reflectable
public record CompatibilityObject(
    @JsonProperty("compatibility") @NotNull String compatibility) {

    /**
     * Gets the compatibility level.
     *
     * @return a compatibility level string.
     */
    @Override
    @JsonProperty("compatibility")
    public String compatibility() {
        return compatibility;
    }

}
