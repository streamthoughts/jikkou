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
package io.streamthoughts.jikkou.schema.registry.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public enum SchemaType {

    AVRO, PROTOBUF, JSON, INVALID;

    @JsonCreator
    public static SchemaType getForNameIgnoreCase(final @Nullable String str) {
        if (str == null) return AVRO;
        return Arrays.stream(SchemaType.values())
                .filter(e -> e.name().equals(str.toUpperCase(Locale.ROOT)))
                .findFirst()
                .orElse(SchemaType.INVALID);
    }

    public static SchemaType defaultType() {
        return SchemaType.AVRO;
    }

}
