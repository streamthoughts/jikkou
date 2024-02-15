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
