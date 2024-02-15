/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wraps a type and a value.
 *
 * @param type  the data type.
 * @param data  the data value.
 */
@Reflectable
public record DataValue(@NotNull DataType type, @Nullable DataHandle data) {

    @ConstructorProperties({
            "type",
            "data",
    })
    public DataValue {}

    @JsonProperty("type")
    @Override
    public DataType type() {
        return type;
    }

    @JsonProperty("data")
    @Override
    public DataHandle data() {
        return Optional.ofNullable(data).orElse(DataHandle.NULL);
    }
}
