/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.connect.change;

import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Change;
import io.jikkou.core.reconciler.change.ChangeComputer;
import io.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.jikkou.kafka.connect.models.KafkaConnectorState;
import io.jikkou.kafka.connect.models.V1KafkaConnector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public final class KafkaConnectorChangeComputer extends ResourceChangeComputer<String, V1KafkaConnector> {

    public static final String DATA_CONNECTOR_CLASS = "connectorClass";
    public static final String DATA_TASKS_MAX = "tasksMax";
    public static final String DATA_STATE = "state";
    public static final String DATA_CONFIG_PREFIX = "config.";

    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     */
    public KafkaConnectorChangeComputer() {
        this(null);
    }

    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     *
     * @param maskedValuePattern the pattern matching config values masked by the Kafka Connect REST API,
     *                           or {@code null} to compare all values as-is.
     */
    public KafkaConnectorChangeComputer(@Nullable Pattern maskedValuePattern) {
        super(object -> object.getMetadata().getName(), new KafkaConnectorChangeFactory(maskedValuePattern));
    }

    public static class KafkaConnectorChangeFactory extends ResourceChangeFactory<String, V1KafkaConnector> {

        private final Pattern maskedValuePattern;

        public KafkaConnectorChangeFactory() {
            this(null);
        }

        public KafkaConnectorChangeFactory(@Nullable Pattern maskedValuePattern) {
            this.maskedValuePattern = maskedValuePattern;
        }

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
            // Compute change for 'state'. The state is optional: when an existing connector declares no desired
            // state, keep its current one rather than reporting a change that no API call can apply.
            if (before != null && after != null && getState(after) == null) {
                changes.add(StateChange.none(DATA_STATE, getState(before)));
            } else {
                changes.add(StateChange.with(DATA_STATE, getState(before), getState(after)));
            }
            // Compute change for 'config'
            Map<String, Object> afterConfig = getConfig(after);
            Map<String, Object> beforeConfig = unmaskConfig(getConfig(before), afterConfig);
            changes.addAll(ChangeComputer.computeChanges(beforeConfig, afterConfig, true)
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

        /**
         * Some Kafka Connect distributions hide sensitive values (e.g. passwords) behind a mask when the
         * connector config is read back. The real value cannot be known, so a masked value is assumed to be
         * equal to the desired one rather than being reported as a permanent drift.
         */
        private Map<String, Object> unmaskConfig(Map<String, Object> actual, Map<String, Object> expected) {
            if (maskedValuePattern == null || actual.isEmpty() || expected.isEmpty()) {
                return actual;
            }
            Map<String, Object> unmasked = new HashMap<>(actual);
            actual.forEach((key, value) -> {
                if (value instanceof String str
                    && expected.containsKey(key)
                    && maskedValuePattern.matcher(str).matches()) {
                    unmasked.put(key, expected.get(key));
                }
            });
            return unmasked;
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
