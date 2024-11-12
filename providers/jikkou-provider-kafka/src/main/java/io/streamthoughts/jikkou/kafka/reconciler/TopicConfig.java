/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.kafka.model.DataType;

public interface TopicConfig {

    ConfigProperty<String> TOPIC_NAME = ConfigProperty.ofString("topic-name")
        .description("The topic name to consume on.")
        .required(true);

    ConfigProperty<DataType> KEY_TYPE = ConfigProperty.ofEnum("key-type", DataType.class)
        .description("The record key type. Valid values: ${COMPLETION-CANDIDATES}.")
        .required(true);

    ConfigProperty<DataType> VALUE_TYPE = ConfigProperty.ofEnum("value-type", DataType.class)
        .description("The record value type. Valid values: ${COMPLETION-CANDIDATES}.")
        .required(true);

    ConfigProperty<Boolean> SKIP_MESSAGE_ON_ERROR = ConfigProperty.ofBoolean("skip-message-on-error")
        .description("If there is an error when processing a message, skip it instead of halt.")
        .defaultValue(false)
        .required(false);
}
