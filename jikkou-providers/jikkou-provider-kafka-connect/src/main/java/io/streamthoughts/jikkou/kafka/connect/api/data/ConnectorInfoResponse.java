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
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import jakarta.validation.constraints.NotNull;
import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * ConnectorInfo HTTP response.
 *
 * @param name   the name of the connector.
 * @param config the configuration of the connector.
 * @param tasks  the tasks of the connector.
 * @see io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi#getConnector(String)
 * @see io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi#createOrUpdateConnector(String, Map)
 */
@Reflectable
public record ConnectorInfoResponse(@JsonProperty("name") @NotNull String name,
                                    @JsonProperty("config") @NotNull Map<String, String> config,
                                    @JsonProperty("tasks") @NotNull List<ConnectorTask> tasks
) implements Serializable {

    @ConstructorProperties({
            "name",
            "config",
            "tasks"
    })
    public ConnectorInfoResponse {
    }

    @Reflectable
    public record ConnectorTask(@JsonProperty("connector") @NotNull String connector,
                                @JsonProperty("task") int task) implements Serializable {

        @ConstructorProperties({
                "connector",
                "task"
        })
        public ConnectorTask {
        }
    }

}
