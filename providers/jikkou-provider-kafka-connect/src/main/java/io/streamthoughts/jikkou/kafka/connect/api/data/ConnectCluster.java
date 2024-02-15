/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi;
import jakarta.validation.constraints.NotNull;
import java.beans.ConstructorProperties;
import java.io.Serializable;

/**
 * ConnectCluster HTTP response.
 *
 * @param version        Connect worker version
 * @param commit         Git commit ID
 * @param kafkaClusterId Kafka cluster ID
 * @see KafkaConnectApi#getConnectCluster()
 */
@Reflectable
public record ConnectCluster(@JsonProperty("version") @NotNull String version,
                             @JsonProperty("commit") @NotNull String commit,
                             @JsonProperty("kafka_cluster_id") @NotNull String kafkaClusterId) implements Serializable {

    /**
     * Creates a new {@link ConnectCluster} instance.
     *
     * @param version        Connect worker version
     * @param commit         Git commit ID
     * @param kafkaClusterId Kafka cluster ID
     */
    @ConstructorProperties({
            "version",
            "commit",
            "kafka_cluster_id"
    })
    public ConnectCluster {

    }
}
