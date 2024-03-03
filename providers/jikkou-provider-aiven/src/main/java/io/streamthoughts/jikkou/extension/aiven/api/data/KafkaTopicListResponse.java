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

@Reflectable
public record KafkaTopicListResponse(
    @JsonProperty("topics") List<KafkaTopicInfoGet> topics,
    @JsonProperty("errors") List<Error> errors,
    @JsonProperty("message") String message){

    /**
     *
     */
    @Reflectable
    public record KafkaTopicInfoGet(
        @JsonProperty("topic_name") String topicName,
        @JsonProperty("partitions") Integer partitions,
        @JsonProperty("replication") Integer replication,
        @JsonProperty("state") State state,
        @JsonProperty("tags") List<Tag> tags
    ) {

    }

}
