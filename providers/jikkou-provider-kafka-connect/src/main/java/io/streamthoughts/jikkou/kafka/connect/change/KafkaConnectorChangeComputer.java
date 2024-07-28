/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.change;

import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Change;
import io.streamthoughts.jikkou.core.reconciler.change.ChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.streamthoughts.jikkou.kafka.connect.models.KafkaConnectorState;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class KafkaConnectorChangeComputer extends ResourceChangeComputer<String, V1KafkaConnector, ResourceChange> {

    public static final String DATA_CONNECTOR_CLASS = "connectorClass";
    public static final String DATA_TASKS_MAX = "tasksMax";
    public static final String DATA_STATE = "state";
    public static final String DATA_CONFIG_PREFIX = "config.";

    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     */
    public KafkaConnectorChangeComputer() {
        super(object -> object.getMetadata().getName(), new KafkaConnectorChangeFactory());
    }

    public static class KafkaConnectorChangeFactory extends ResourceChangeFactory<String, V1KafkaConnector, ResourceChange> {

        @Override
        public ResourceChange createChangeForCreate(String key, V1KafkaConnector after) {
            return createChangeForUpdate(key, null, after);
        }

        @Override
        public ResourceChange createChangeForDelete(String key, V1KafkaConnector before) {
            return createChangeForUpdate(key, before, null);
        }

        @Override
        public ResourceChange createChangeForUpdate(String key,
                                                    V1KafkaConnector before,
                                                    V1KafkaConnector after) {
            List<StateChange> changes = new ArrayList<>();
            // Compute change for 'connector.class'
            changes.add(StateChange.with(DATA_CONNECTOR_CLASS, getConnectorClass(before), getConnectorClass(after)));
            // Compute change for 'tasks.max'
            changes.add(StateChange.with(DATA_TASKS_MAX, getTasksMax(before), getTasksMax(after)));
            // Compute change for 'state'
            changes.add(StateChange.with(DATA_STATE, getState(before), getState(after)));
            // Compute change for 'config'
            changes.addAll(ChangeComputer.computeChanges(getConfig(before), getConfig(after), true)
                .stream()
                .map(change -> change.withName(DATA_CONFIG_PREFIX + change.getName()))
                .toList()
            );
            return GenericResourceChange
                .builder(V1KafkaConnector.class)
                .withMetadata(Optional.ofNullable(after).orElse(before).getMetadata())
                .withSpec(ResourceChangeSpec
                    .builder()
                    .withOperation(Change.computeOperation(changes))
                    .withChanges(changes)
                    .build()
                )
                .build();
        }
    }

    private static Map<String, Object> getConfig(V1KafkaConnector connector) {
        return Optional.ofNullable(connector).map(o -> o.getSpec().getConfig()).orElse(Collections.emptyMap());
    }

    private static KafkaConnectorState getState(V1KafkaConnector connector) {
        return Optional.ofNullable(connector).map(o -> o.getSpec().getState()).orElse(null);
    }

    private static Integer getTasksMax(V1KafkaConnector connector) {
        return Optional.ofNullable(connector).map(o -> o.getSpec().getTasksMax()).orElse(null);
    }

    private static String getConnectorClass(V1KafkaConnector connector) {
        return Optional.ofNullable(connector).map(o -> o.getSpec().getConnectorClass()).orElse(null);
    }
}
