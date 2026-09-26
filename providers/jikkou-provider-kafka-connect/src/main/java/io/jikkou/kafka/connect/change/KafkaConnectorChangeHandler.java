/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.connect.change;

import static io.jikkou.kafka.connect.KafkaConnectConstants.CONNECTOR_CLASS_CONFIG;
import static io.jikkou.kafka.connect.KafkaConnectConstants.CONNECTOR_TASKS_MAX_CONFIG;
import static io.jikkou.kafka.connect.change.KafkaConnectorChangeComputer.DATA_CONNECTOR_CLASS;

import io.jikkou.core.data.TypeConverter;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.SpecificStateChange;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Change;
import io.jikkou.core.reconciler.ChangeError;
import io.jikkou.core.reconciler.ChangeMetadata;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import io.jikkou.core.reconciler.change.BaseChangeHandler;
import io.jikkou.kafka.connect.api.KafkaConnectApi;
import io.jikkou.kafka.connect.api.data.ConnectorCreateRequest;
import io.jikkou.kafka.connect.api.data.ConnectorInfoResponse;
import io.jikkou.kafka.connect.api.data.ErrorResponse;
import io.jikkou.kafka.connect.models.KafkaConnectorState;
import jakarta.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

public final class KafkaConnectorChangeHandler extends BaseChangeHandler {

    private final KafkaConnectApi api;
    private final String cluster;

    /**
     * Creates a new {@link KafkaConnectorChangeHandler} instance.
     *
     * @param api the KafkaConnect client.
     */
    public KafkaConnectorChangeHandler(@NotNull KafkaConnectApi api, @NotNull String cluster) {
        super(Set.of(Operation.CREATE, Operation.DELETE, Operation.UPDATE));
        this.api = Objects.requireNonNull(api);
        this.cluster = cluster;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {
        return changes.stream().flatMap(this::handleChange).toList();
    }

    private Stream<ChangeResponse> handleChange(ResourceChange change) {
        return switch (change.getSpec().getOp()) {
            case NONE -> Stream.empty(); // no change of these types should be handled by this class.
            case REPLACE -> Stream.empty(); // connectors are never replaced.
            case UPDATE -> updateConnector(change);
            case CREATE -> createConnector(change);
            case DELETE -> deleteConnector(change);
        };
    }

    /**
     * Updates an existing connector: its configuration using PUT /connectors/{name}/config and/or
     * its target state using the pause, stop and resume endpoints.
     */
    @NotNull
    private Stream<ChangeResponse> updateConnector(ResourceChange change) {
        final String connectorName = change.getMetadata().getName();
        final SpecificStateChange<KafkaConnectorState> stateChange = getState(change);
        final KafkaConnectorState desiredState = stateChange.getOp() != Operation.NONE ? stateChange.getAfter() : null;

        final boolean configChanged = hasConfigChange(change);
        final Runnable updateConfig = () -> api.createOrUpdateConnector(connectorName, buildConnectorConfig(change));
        final List<Runnable> actions = new ArrayList<>();

        // PUT /connectors/{name}/config never changes the target state of a connector, so a state transition
        // must be sent explicitly. Pause or stop the connector before updating its config so that tasks never
        // run with the new config, and resume it only once the new config is applied.
        switch (desiredState) {
            case PAUSED -> {
                actions.add(() -> api.pauseConnector(connectorName));
                if (configChanged) actions.add(updateConfig);
            }
            case STOPPED -> {
                actions.add(() -> api.stopConnector(connectorName));
                if (configChanged) actions.add(updateConfig);
            }
            case RUNNING -> {
                if (configChanged) actions.add(updateConfig);
                actions.add(() -> api.resumeConnector(connectorName));
            }
            case UNASSIGNED, RESTARTING, FAILED -> {
                return Stream.of(toChangeResponse(change, CompletableFuture.failedFuture(new IllegalArgumentException(
                    String.format(
                        "Cannot transition connector '%s' to state %s: only RUNNING, PAUSED and STOPPED are supported",
                        connectorName, desiredState)
                ))));
            }
            case null -> {
                if (configChanged) actions.add(updateConfig);
            }
        }

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> actions.forEach(Runnable::run));
        return Stream.of(toChangeResponse(change, future));
    }

    @NotNull
    private Stream<ChangeResponse> deleteConnector(ResourceChange change) {
        CompletableFuture<Void> future = CompletableFuture
                .runAsync(() -> api.deleteConnector(change.getMetadata().getName()));
        return Stream.of(toChangeResponse(change, future));
    }

    /**
     * Creates a new connector using POST /connectors with initial_state support (KIP-980).
     */
    @NotNull
    private Stream<ChangeResponse> createConnector(ResourceChange change) {
        final String connectorName = change.getMetadata().getName();
        final Map<String, Object> configAsMap = buildConnectorConfig(change);
        final SpecificStateChange<KafkaConnectorState> stateChange = getState(change);
        final KafkaConnectorState desiredState = stateChange.getAfter();

        // Determine the initial_state parameter for KIP-980
        // Only RUNNING, STOPPED, and PAUSED are valid initial states
        final String initialState = switch (desiredState) {
            case STOPPED, PAUSED -> desiredState.value();
            // For RUNNING or other states (including null), don't set initial_state (defaults to RUNNING)
            case RUNNING, UNASSIGNED, RESTARTING, FAILED -> null;
            case null -> null;
        };

        ConnectorCreateRequest request = new ConnectorCreateRequest(connectorName, configAsMap, initialState);
        CompletableFuture<ConnectorInfoResponse> future = CompletableFuture.supplyAsync(() ->
                api.createConnector(request)
        );

        ChangeResponse response = toChangeResponse(change, future);
        return Stream.of(response);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public TextDescription describe(@NotNull ResourceChange change) {
        return new KafkaConnectorChangeDescription(cluster, change);
    }

    @VisibleForTesting
    static boolean hasConfigChange(ResourceChange change) {
        return getConnectorClass(change).getOp() != Operation.NONE
            || getTasksMax(change).getOp() != Operation.NONE
            || Change.computeOperation(getConfig(change)) != Operation.NONE;
    }

    private Map<String, Object> buildConnectorConfig(final ResourceChange change) {
        Map<String, Object> configs = getConfig(change)
            .stream()
            .filter(state -> state.getOp() != Operation.DELETE && state.getAfter() != null)
            .collect(Collectors.toMap(StateChange::getName, StateChange::getAfter));
        Map<String, Object> config = new HashMap<>();
        config.put(CONNECTOR_TASKS_MAX_CONFIG, getTasksMax(change).getAfter());
        config.put(CONNECTOR_CLASS_CONFIG, getConnectorClass(change).getAfter());
        config.putAll(configs);
        return config;
    }

    private static List<StateChange> getConfig(ResourceChange change) {
        return change.getSpec().getChanges()
                .allWithPrefix(KafkaConnectorChangeComputer.DATA_CONFIG_PREFIX)
                .all();
    }

    private static SpecificStateChange<KafkaConnectorState> getState(ResourceChange change) {
        return change
                .getSpec()
                .getChanges()
                .getLast(KafkaConnectorChangeComputer.DATA_STATE, TypeConverter.of(KafkaConnectorState.class));
    }

    private static SpecificStateChange<String> getConnectorClass(ResourceChange change) {
        return change.getSpec().getChanges().getLast(DATA_CONNECTOR_CLASS, TypeConverter.String());
    }

    private static SpecificStateChange<Integer> getTasksMax(ResourceChange change) {
        return change.getSpec().getChanges().getLast(KafkaConnectorChangeComputer.DATA_TASKS_MAX, TypeConverter.Integer());
    }

    private ChangeResponse toChangeResponse(ResourceChange change, CompletableFuture<?> future) {
        CompletableFuture<ChangeMetadata> handled = future.handle((unused, throwable) -> {
            if (throwable == null) {
                return ChangeMetadata.empty();
            }

            if (throwable.getCause() != null) {
                throwable = throwable.getCause();
            }

            if (throwable instanceof WebApplicationException e) {
                ErrorResponse error = e.getResponse().readEntity(ErrorResponse.class);
                return new ChangeMetadata(new ChangeError(error.message(), error.errorCode()));
            }
            return ChangeMetadata.of(throwable);
        });

        return new ChangeResponse(change, handled);
    }


}
