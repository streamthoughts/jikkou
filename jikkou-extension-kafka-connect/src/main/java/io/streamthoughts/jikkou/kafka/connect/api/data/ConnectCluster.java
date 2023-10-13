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
package io.streamthoughts.jikkou.kafka.connect.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.annotation.Reflectable;
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
