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
package io.streamthoughts.jikkou.kafka.connect.change;

import static io.streamthoughts.jikkou.kafka.connect.KafkaConnectConstants.CONNECTOR_CLASS_CONFIG;
import static io.streamthoughts.jikkou.kafka.connect.KafkaConnectConstants.CONNECTOR_TASKS_MAX_CONFIG;

import io.streamthoughts.jikkou.core.change.ChangeDescription;
import io.streamthoughts.jikkou.core.change.ChangeError;
import io.streamthoughts.jikkou.core.change.ChangeHandler;
import io.streamthoughts.jikkou.core.change.ChangeMetadata;
import io.streamthoughts.jikkou.core.change.ChangeResponse;
import io.streamthoughts.jikkou.core.change.ChangeType;
import io.streamthoughts.jikkou.core.change.ConfigEntryChange;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi;
import io.streamthoughts.jikkou.kafka.connect.api.data.ConnectorInfoResponse;
import io.streamthoughts.jikkou.kafka.connect.api.data.ErrorResponse;
import io.streamthoughts.jikkou.kafka.connect.models.KafkaConnectorState;
import io.streamthoughts.jikkou.rest.client.RestClientException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public final class KafkaConnectorChangeHandler implements ChangeHandler<KafkaConnectorChange> {

    private final KafkaConnectApi api;
    private final String cluster;

    /**
     * Creates a new {@link KafkaConnectorChangeHandler} instance.
     *
     * @param api the KafkaConnect client.
     */
    public KafkaConnectorChangeHandler(@NotNull KafkaConnectApi api, @NotNull String cluster) {
        this.api = Objects.requireNonNull(api);
        this.cluster = cluster;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Set<ChangeType> supportedChangeTypes() {
        return Set.of(ChangeType.ADD, ChangeType.DELETE, ChangeType.UPDATE);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResponse<KafkaConnectorChange>> apply(@NotNull List<HasMetadataChange<KafkaConnectorChange>> items) {
        return items.stream().flatMap(this::handleChange).toList();
    }

    private Stream<ChangeResponse<KafkaConnectorChange>> handleChange(HasMetadataChange<KafkaConnectorChange> item) {
        KafkaConnectorChange change = item.getChange();
        return switch (change.operation()) {
            case NONE, IGNORE -> Stream.empty(); // no change of these types should be handled by this class.
            case UPDATE -> updateConnector(item);
            case ADD -> createOrUpdateConnectorConfig(item);
            case DELETE -> deleteConnector(item);
        };
    }

    @NotNull
    private Stream<ChangeResponse<KafkaConnectorChange>> updateConnector(HasMetadataChange<KafkaConnectorChange> item) {
        KafkaConnectorChange change = item.getChange();
        if (!change.isStateOnlyChange())
            return createOrUpdateConnectorConfig(item);

        KafkaConnectorState newState = change.state().getAfter();

        String connectorName = change.name();
        CompletableFuture<Void> future = switch (newState) {
            case PAUSED -> CompletableFuture
                    .runAsync(() -> api.pauseConnector(connectorName));
            case STOPPED -> CompletableFuture
                    .runAsync(() -> api.stopConnector(connectorName));
            case RUNNING -> CompletableFuture
                    .runAsync(() -> api.resumeConnector(connectorName));
        };
        ChangeResponse<KafkaConnectorChange> response = toChangeResponse(item, future);
        return Stream.of(response);
    }

    @NotNull
    private Stream<ChangeResponse<KafkaConnectorChange>> deleteConnector(HasMetadataChange<KafkaConnectorChange> item) {
        CompletableFuture<Void> future = CompletableFuture
                .runAsync(() -> api.deleteConnector(item.getChange().name()));

        ChangeResponse<KafkaConnectorChange> response = toChangeResponse(item, future);
        return Stream.of(response);
    }

    @NotNull
    private Stream<ChangeResponse<KafkaConnectorChange>> createOrUpdateConnectorConfig(HasMetadataChange<KafkaConnectorChange> item) {
        CompletableFuture<ConnectorInfoResponse> future = CompletableFuture.supplyAsync(() ->
                api.createOrUpdateConnector(item.getChange().name(), buildConnectorConfig(item.getChange()))
        );

        ChangeResponse<KafkaConnectorChange> response = toChangeResponse(item, future);
        return Stream.of(response);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ChangeDescription getDescriptionFor(@NotNull HasMetadataChange<KafkaConnectorChange> item) {
        return new KafkaConnectorChangeDescription(cluster, item.getChange());
    }

    private Map<String, Object> buildConnectorConfig(KafkaConnectorChange change) {
        Map<String, Object> config = new HashMap<>();
        config.put(CONNECTOR_TASKS_MAX_CONFIG, change.tasksMax().getAfter());
        config.put(CONNECTOR_CLASS_CONFIG, change.connectorClass().getAfter());
        config.putAll(change.config().stream()
                .filter(it -> it.operation() != ChangeType.DELETE)
                .collect(Collectors.toMap(ConfigEntryChange::name, it -> it.valueChange().getAfter()))
        );
        return config;
    }

    private ChangeResponse<KafkaConnectorChange> toChangeResponse(HasMetadataChange<KafkaConnectorChange> change,
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
