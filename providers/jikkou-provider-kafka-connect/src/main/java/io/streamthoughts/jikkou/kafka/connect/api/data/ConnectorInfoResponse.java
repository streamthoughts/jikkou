/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
