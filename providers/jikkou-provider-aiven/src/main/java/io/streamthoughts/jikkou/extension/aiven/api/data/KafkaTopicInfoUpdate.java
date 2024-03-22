/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;


import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Reflectable
public record KafkaTopicInfoUpdate(
    String topicName,
    Integer partitions,
    Integer replication,
    Map<String, Object> configs,
    List<Tag> tags
) {

    @ConstructorProperties(
        {
            "topic_name",
            "partitions",
            "replication",
            "configs",
            "tags"
        }
    )
    public KafkaTopicInfoUpdate {

    }
}
