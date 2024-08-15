/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.change;

import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Change;
import io.streamthoughts.jikkou.core.reconciler.ChangeError;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import io.streamthoughts.jikkou.http.client.RestClientException;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi;
import io.streamthoughts.jikkou.kafka.connect.api.data.ConnectorInfoResponse;
import io.streamthoughts.jikkou.kafka.connect.api.data.ErrorResponse;
import io.streamthoughts.jikkou.kafka.connect.models.KafkaConnectorState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.streamthoughts.jikkou.kafka.connect.KafkaConnectConstants.CONNECTOR_CLASS_CONFIG;
import static io.streamthoughts.jikkou.kafka.connect.KafkaConnectConstants.CONNECTOR_TASKS_MAX_CONFIG;
import static io.streamthoughts.jikkou.kafka.connect.change.KafkaConnectorChangeComputer.DATA_CONNECTOR_CLASS;

public final class KafkaConnectorChangeHandler extends BaseChangeHandler<ResourceChange> {

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
    public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull List<ResourceChange> changes) {
        return changes.stream().flatMap(this::handleChange).toList();
    }

    private Stream<ChangeResponse<ResourceChange>> handleChange(ResourceChange change) {
        return switch (change.getSpec().getOp()) {
            case NONE -> Stream.empty(); // no change of these types should be handled by this class.
            case UPDATE -> updateConnector(change);
            case CREATE -> createOrUpdateConnectorConfig(change);
            case DELETE -> deleteConnector(change);
        };
    }

    @NotNull
    private Stream<ChangeResponse<ResourceChange>> updateConnector(ResourceChange change) {
        if (!isStateOnlyChange(change)) {
            return createOrUpdateConnectorConfig(change);
        }
        SpecificStateChange<KafkaConnectorState> stateChange = getState(change);

        KafkaConnectorState newState = stateChange.getAfter();

        String connectorName = change.getMetadata().getName();
        Optional<CompletableFuture<Void>> future = switch (newState) {
            case PAUSED -> Optional.of(CompletableFuture
                    .runAsync(() -> api.pauseConnector(connectorName)));
            case STOPPED -> Optional.of(CompletableFuture
                    .runAsync(() -> api.stopConnector(connectorName)));
            case RUNNING -> Optional.of(CompletableFuture
                    .runAsync(() -> api.resumeConnector(connectorName)));
            // new state cannot be one of:
            case UNASSIGNED, RESTARTING, FAILED -> Optional.empty();
        };

        return future.map(f -> toChangeResponse(change, f)).stream();
    }


    @NotNull
    private Stream<ChangeResponse<ResourceChange>> deleteConnector(ResourceChange change) {
        CompletableFuture<Void> future = CompletableFuture
                .runAsync(() -> api.deleteConnector(change.getMetadata().getName()));

        ChangeResponse<ResourceChange> response = toChangeResponse(change, future);
        return Stream.of(response);
    }

    @NotNull
    private Stream<ChangeResponse<ResourceChange>> createOrUpdateConnectorConfig(ResourceChange change) {
        final Map<String, Object> configAsMap = buildConnectorConfig(change);
        CompletableFuture<ConnectorInfoResponse> future = CompletableFuture.supplyAsync(() ->
                api.createOrUpdateConnector(change.getMetadata().getName(), configAsMap)
        );

        ChangeResponse<ResourceChange> response = toChangeResponse(change, future);
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
    static boolean isStateOnlyChange(ResourceChange change) {
        if (change.getSpec().getOp() != Operation.UPDATE)
            return false;

        if (getConnectorClass(change).getOp() != Operation.NONE)
            return false;

        if (getTasksMax(change).getOp() != Operation.NONE)
            return false;

        if (Change.computeOperation(getConfig(change)) != Operation.NONE)
            return false;

        return getState(change).getOp() != Operation.NONE;
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

    private ChangeResponse<ResourceChange> toChangeResponse(ResourceChange change,
                                                            CompletableFuture<?> future) {
        CompletableFuture<ChangeMetadata> handled = future.handle((unused, throwable) -> {
            if (throwable == null) {
                return ChangeMetadata.empty();
            }

            if (throwable.getCause() != null) {
                throwable = throwable.getCause();
            }

            if (throwable instanceof RestClientException e) {
                ErrorResponse error = e.getResponseEntity(ErrorResponse.class);
                return new ChangeMetadata(new ChangeError(error.message(), error.errorCode()));
            }
            return ChangeMetadata.of(throwable);
        });

        return new ChangeResponse<>(change, handled);
    }


}
