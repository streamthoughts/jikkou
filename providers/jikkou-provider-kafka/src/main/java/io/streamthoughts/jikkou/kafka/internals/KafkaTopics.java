/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals;

import java.util.Set;
import org.apache.kafka.common.internals.Topic;
import org.jetbrains.annotations.NotNull;

public final class KafkaTopics {

    public static final Integer NO_NUM_PARTITIONS = -1;
    public static final Short NO_REPLICATION_FACTOR = -1;

    // List of known internal topics for Apache Kafka, Kafka Connect, Kafka Streams, and
    public static final Set<String> INTERNAL_TOPICS = Set.of(
            "_schemas",
            "connect-offsets",
            "connect-status",
            "connect-configs"
    );
    
    private static final String INTERNAL_TOPIC_PREFIX = "__";

    /** Check topic is kafka internal topic. */
    public static boolean isInternalTopic(@NotNull final String topic) {
        return isMM2InternalTopic(topic) || isDefaultKafkaInternalTopic(topic) || isDefaultConnectTopic(topic);
    }

    /** Check topic is default kafka internal topic. */
    private static boolean isDefaultKafkaInternalTopic(String topic) {
        return Topic.isInternal(topic) || topic.startsWith(INTERNAL_TOPIC_PREFIX) || topic.startsWith(".") || INTERNAL_TOPICS.contains(topic);
    }

    /** Check topic is default connect internal topic. */
    private static boolean isDefaultConnectTopic(String topic) {
        return topic.endsWith("-internal") ||  topic.endsWith(".internal");
    }

    /** Check topic is one of MM2 internal topic. */
    private static boolean isMM2InternalTopic(String topic) {
        return topic.endsWith(".internal");
    }

    private KafkaTopics() {}
}
