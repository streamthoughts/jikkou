/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Reflectable
public record KafkaTopicInfo(
    @JsonProperty("topic_name") String topicName,
    @JsonProperty("partitions") List<PartitionInfo> partitions,
    @JsonProperty("replication") Integer replication,
    @JsonProperty("config") Map<String, KafkaTopicConfigInfo> config,
    @JsonProperty("state") State state,
    @JsonProperty("tags") List<Tag> tags
) {


}
