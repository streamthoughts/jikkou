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

/**
 * ConnectorStatus HTTP-response.
 *
 * @param name      the name of the connector.
 * @param connector the status of the connector.
 * @param tasks     the status of tasks.
 * @see io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi#getConnectorStatus(String)
 */
@Reflectable
public record ConnectorStatusResponse(@JsonProperty("name") @NotNull String name,
                                      @JsonProperty("connector") @NotNull ConnectorStatus connector,
                                      @JsonProperty("tasks") @NotNull List<TaskStatus> tasks) implements Serializable {

    @ConstructorProperties({"name", "connector", "tasks"})
    public ConnectorStatusResponse {
    }


    /**
     * Status of a connector.
     *
     * @param state    the state of the connector (i.e, RUNNING, STOPPED, PAUSED, FAILED)
     * @param workerId the worker id.
     */
    @Reflectable
    public record ConnectorStatus(@NotNull String state, @NotNull String workerId) {

        @ConstructorProperties({"state", "worker_id"})
        public ConnectorStatus {
        }
    }

    /**
     * Status of a connector-task
     *
     * @param id       the task-id.
     * @param state    the state of the task (i.e, RUNNING, STOPPED, PAUSED, FAILED)
     * @param workerId the worker id.
     */
    @Reflectable
    public record TaskStatus(int id, @NotNull String state, @NotNull String workerId) {

        @ConstructorProperties({"id", "state", "worker_id"})
        public TaskStatus {
        }
    }
}
