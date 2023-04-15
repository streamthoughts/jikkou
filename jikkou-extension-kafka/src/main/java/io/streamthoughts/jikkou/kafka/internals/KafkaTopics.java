/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.internals;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class KafkaTopics {

    public static final Integer NO_NUM_PARTITIONS = -1;
    public static final Short NO_REPLICATION_FACTOR = -1;

    public static final Set<String> INTERNAL_TOPICS = Set.of(
            "_schemas",
            "__consumer_offsets",
            "__transaction_state",
            "connect-offsets",
            "connect-status",
            "connect-configs"
    );

    /** Check topic is kafka internal topic. */
    public static boolean isInternalTopic(@NotNull final String topic) {
        return isMM2InternalTopic(topic) || isDefaultKafkaInternalTopic(topic) || isDefaultConnectTopic(topic);
    }

    /** Check topic is default kafka internal topic. */
    private static boolean isDefaultKafkaInternalTopic(String topic) {
        return topic.startsWith("__") || topic.startsWith(".") || INTERNAL_TOPICS.contains(topic);
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
