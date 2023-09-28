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
package io.streamthoughts.jikkou.kafka.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.annotation.Reflectable;
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
