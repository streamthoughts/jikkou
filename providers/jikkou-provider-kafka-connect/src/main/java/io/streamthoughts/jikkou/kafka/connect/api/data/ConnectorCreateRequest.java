/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.api.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * Request body for creating a new connector via POST /connectors.
 *
 * @param name         the name of the connector.
 * @param config       the configuration of the connector.
 * @param initialState the initial state of the connector (RUNNING, STOPPED, or PAUSED).
 *                     This field is optional; if not specified, the connector starts in RUNNING state.
 * @see io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi#createConnector(ConnectorCreateRequest)
 */
@Reflectable
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConnectorCreateRequest(@JsonProperty("name") @NotNull String name,
                                     @JsonProperty("config") @NotNull Map<String, Object> config,
                                     @JsonProperty("initial_state") String initialState
) implements Serializable {

    /**
     * Creates a ConnectorCreateRequest with default RUNNING state.
     *
     * @param name   the connector name.
     * @param config the connector configuration.
     */
    public ConnectorCreateRequest(String name, Map<String, Object> config) {
        this(name, config, null);
    }
}