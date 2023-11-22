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
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;

/**
 * Represents a string key/value record header.
 *
 * @param name    of the header. Must not be {@code null}.
 * @param value   of the header. Must not be {@code null}.
 */
@Reflectable
public record KafkaRecordHeader(String name, String value) {

    @ConstructorProperties({
            "name",
            "value",
    })
    public KafkaRecordHeader {}

    @JsonProperty("name")
    public String name() {
        return name;
    }

    @Override
    @JsonProperty("value")
    public String value() {
        return value;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "(name=" + name + ", value='" + value + ')';
    }
}
