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
package io.streamthoughts.jikkou.kafka.connect.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The state the connector should be in. Defaults to running.
 */
public enum KafkaConnectorState {

    PAUSED,
    STOPPED,
    RUNNING;

    private final static Map<String, KafkaConnectorState> CONSTANTS = new HashMap<>();

    static {
        for (KafkaConnectorState c: values()) {
            CONSTANTS.put(c.value(), c);
        }
    }

    KafkaConnectorState() {}

    @Override
    public String toString() {
        return this.name();
    }

    @JsonValue
    public String value() {
        return this.name();
    }

    @JsonCreator
    public static KafkaConnectorState fromValue(String value) {
        KafkaConnectorState constant = CONSTANTS.get(value.toUpperCase(Locale.ROOT));
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }
}
