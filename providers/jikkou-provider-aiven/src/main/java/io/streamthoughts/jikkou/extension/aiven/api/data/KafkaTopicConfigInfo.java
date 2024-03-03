/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.common.utils.Enums;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import org.jetbrains.annotations.Nullable;

@Reflectable
public record KafkaTopicConfigInfo(@JsonProperty("source") Source source,
                                   @JsonProperty("value") Object value) {

    @Reflectable
    public enum Source {
        UNKNOWN_CONFIG,
        TOPIC_CONFIG,
        DYNAMIC_BROKER_CONFIG,
        DYNAMIC_DEFAULT_BROKER_CONFIG,
        STATIC_BROKER_CONFIG,
        DEFAULT_CONFIG,
        DYNAMIC_BROKER_LOGGER_CONFIG;

        @JsonCreator
        public static Source getForNameIgnoreCase(final @Nullable String str) {
            return Enums.getForNameIgnoreCase(str, Source.class);
        }
    }
}