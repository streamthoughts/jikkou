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
package io.streamthoughts.jikkou.kafka.change.handlers.consumer;

import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.reconcilier.ChangeDescription;
import io.streamthoughts.jikkou.core.reconcilier.ChangeHandler;
import io.streamthoughts.jikkou.core.reconcilier.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResponse;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.kafka.internals.Futures;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteConsumerGroupsResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteConsumerGroupHandler implements ChangeHandler<ValueChange<V1KafkaConsumerGroup>> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteConsumerGroupHandler.class);

    private final AdminClient client;

    /**
     * Creates a new {@link DeleteConsumerGroupHandler} instance.
     *
     * @param client The AdminClient.
     */
    public DeleteConsumerGroupHandler(@NotNull AdminClient client) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Set<ChangeType> supportedChangeTypes() {
        return Set.of(ChangeType.DELETE);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResponse<ValueChange<V1KafkaConsumerGroup>>> apply(@NotNull List<HasMetadataChange<ValueChange<V1KafkaConsumerGroup>>> items) {

        Map<String, HasMetadataChange<ValueChange<V1KafkaConsumerGroup>>> consumerGroupsByName = items
                .stream()
                .collect(Collectors.toMap(
                        resource -> resource.getMetadata().getName(),
                        Function.identity()
                ));

        DeleteConsumerGroupsResult result = client.deleteConsumerGroups(consumerGroupsByName.keySet());
        Map<String, CompletableFuture<Void>> futuresByConsumerGroup = result.deletedGroups()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Futures.toCompletableFuture(e.getValue())));

        return futuresByConsumerGroup
                .entrySet()
                .stream()
                .map(e -> {
                    CompletableFuture<ChangeMetadata> future = e.getValue().thenApply(unused -> {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Completed deletion of Kafka Consumer Group {}", e.getKey());
                        }
                        return ChangeMetadata.empty();
                    });
                    HasMetadataChange<ValueChange<V1KafkaConsumerGroup>> item = consumerGroupsByName.get(e.getKey());
                    return new ChangeResponse<>(item, future);
                })
                .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ChangeDescription getDescriptionFor(@NotNull HasMetadataChange<ValueChange<V1KafkaConsumerGroup>> item) {
        return new ConsumerGroupChangeDescription(item);
    }
}
