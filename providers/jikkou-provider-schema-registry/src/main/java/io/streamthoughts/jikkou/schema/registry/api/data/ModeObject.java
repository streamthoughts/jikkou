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
 * ModeObject.
 *
 * @param mode a mode string.
 */
@Reflectable
public record ModeObject(@NotNull String mode) {
    @ConstructorProperties({
            "mode"
    })
    public ModeObject {}

    @Override
    @JsonProperty("mode")
    public String mode() { return mode; }
}
