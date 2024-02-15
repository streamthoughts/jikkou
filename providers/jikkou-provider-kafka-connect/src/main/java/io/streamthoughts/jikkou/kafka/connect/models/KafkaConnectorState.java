/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The state the connector is or should be in. Defaults to RUNNING.
 *
 * see org.apache.kafka.connect.runtime.AbstractStatus.State.
 */
public enum KafkaConnectorState {
    UNASSIGNED,
    RESTARTING,
    FAILED,
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
