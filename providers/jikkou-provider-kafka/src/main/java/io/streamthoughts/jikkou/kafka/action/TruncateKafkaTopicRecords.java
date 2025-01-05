/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.action;

import static io.streamthoughts.jikkou.kafka.reconciler.service.KafkaOffsetSpec.ToTimestamp.fromISODateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.action.*;
import io.streamthoughts.jikkou.core.annotation.*;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.BaseHasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientFactory;
import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaAdminService;
import java.beans.ConstructorProperties;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeletedRecords;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Named(TruncateKafkaTopicRecords.NAME)
@Title("Truncate topic-partitions to a specific datetime.")
@Description("""
    """)
public class TruncateKafkaTopicRecords extends ContextualExtension implements Action<TruncateKafkaTopicRecords.V1TruncatedKafkaTopicRecords> {

    public static final String NAME = "TruncateKafkaTopicRecords";

    private static final Logger LOG = LoggerFactory.getLogger(TruncateKafkaTopicRecords.class);

    public interface Config {
        ConfigProperty<List<String>> TOPIC = ConfigProperty.ofList("topic")
            .description("The topic whose partitions must be truncated.")
            .required(true);

        ConfigProperty<String> TO_DATETIME = ConfigProperty
            .ofString("to-datetime")
            .description("Truncate topic partitions to offsets for datetime. Format: 'YYYY-MM-DDTHH:mm:SS.sss'")
            .required(true);

        ConfigProperty<Boolean> DRY_RUN = ConfigProperty
            .ofBoolean("dry-run")
            .description("Only show results without executing changes on Kafka topics.")
            .defaultValue(false);
    }

    private AdminClientFactory adminClientFactory;

    /**
     * Creates a new {@link TruncateKafkaTopicRecords} instance.
     * Extension requires an empty constructor.
     */
    public TruncateKafkaTopicRecords() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        this.adminClientFactory = context.<KafkaExtensionProvider>provider().newAdminClientFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull ExecutionResultSet<V1TruncatedKafkaTopicRecords> execute(@NotNull Configuration configuration) {
        try (AdminClient client = adminClientFactory.createAdminClient()) {
            KafkaAdminService service = new KafkaAdminService(client);

            final List<String> topics = Config.TOPIC.get(configuration);
            final String dateTime = Config.TO_DATETIME.get(configuration);
            final Long timestamp = fromISODateTime(dateTime).timestamp();


            List<ExecutionResult<V1TruncatedKafkaTopicRecords>> results = Flux.fromIterable(topics)
                .flatMap(topic -> Mono.fromFuture(service.listOffsets(List.of(topic), OffsetSpec.forTimestamp(timestamp)))
                    .flatMap(offsetsByTopicPartition -> {
                        Map<TopicPartition, RecordsToDelete> recordsToDelete = offsetsByTopicPartition.entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    it -> RecordsToDelete.beforeOffset(it.getValue().offset())
                                )
                            );

                        LOG.info("Deleting record for topic '{}' from partition: {}", topic, recordsToDelete);
                        Map<TopicPartition, KafkaFuture<DeletedRecords>> lowWatermarks = client.deleteRecords(recordsToDelete).lowWatermarks();
                        return Flux
                            .fromStream(lowWatermarks.entrySet().stream().map(Pair::of))
                            .flatMap(pair ->
                                Mono.fromFuture(pair._2().toCompletionStage().toCompletableFuture())
                                    .map(deleted -> new TopicPartitionLowWatermark(pair._1().partition(), deleted.lowWatermark()))
                            )
                            .collectSortedList(Comparator.comparingInt(TopicPartitionLowWatermark::partition))
                            .map(topicPartitionLowWatermarks ->
                                ExecutionResult.<V1TruncatedKafkaTopicRecords>newBuilder()
                                    .status(ExecutionStatus.SUCCEEDED)
                                    .data(new V1TruncatedKafkaTopicRecords(new TruncatedKafkaTopicRecordsResult(topic, topicPartitionLowWatermarks)))
                                    .build()
                            );
                    })
                    .onErrorResume(ex ->
                        Mono.just(ExecutionResult.<V1TruncatedKafkaTopicRecords>newBuilder()
                            .status(ExecutionStatus.FAILED)
                            .errors(List.of(new ExecutionError(ex.getLocalizedMessage())))
                            .data(new V1TruncatedKafkaTopicRecords(new TruncatedKafkaTopicRecordsResult(topic, null)))
                            .build()
                        )
                    )
                )
                .collectList()
                .block();

            return ExecutionResultSet.<V1TruncatedKafkaTopicRecords>newBuilder()
                .results(results)
                .build();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(
            Config.TOPIC,
            Config.TO_DATETIME,
            Config.DRY_RUN
        );
    }

    @ApiVersion("kafka.jikkou.io/v1")
    @Kind("TruncatedKafkaTopicRecords")
    @JsonPropertyOrder({
        "apiVersion",
        "kind",
        "metadata",
        "result"
    })
    public static class V1TruncatedKafkaTopicRecords extends BaseHasMetadata {

        private final TruncatedKafkaTopicRecordsResult result;

        public V1TruncatedKafkaTopicRecords(TruncatedKafkaTopicRecordsResult result) {
            this(null, null, null, result);
        }

        @ConstructorProperties({
            "apiVersion",
            "kind",
            "metadata",
            "result"
        })
        public V1TruncatedKafkaTopicRecords(@Nullable String apiVersion,
                                            @Nullable String kind,
                                            @Nullable ObjectMeta metadata,
                                            TruncatedKafkaTopicRecordsResult result) {
            super(apiVersion, kind, metadata);
            this.result = result;
        }

        public TruncatedKafkaTopicRecordsResult getResult() {
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            V1TruncatedKafkaTopicRecords that = (V1TruncatedKafkaTopicRecords) o;
            return Objects.equals(result, that.result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "V1TruncatedKafkaTopicRecords{" +
                "result=" + result +
                '}';
        }
    }

    @JsonPropertyOrder({
        "topic",
        "partitions"
    })
    @Reflectable
    public record TruncatedKafkaTopicRecordsResult(
        @JsonProperty("topic")
        @JsonPropertyDescription("The topic name.")
        String topic,

        @JsonProperty("partitions")
        @JsonPropertyDescription("The topic partitions for which records was deleted.")
        List<TopicPartitionLowWatermark> partitions
    ) {
    }

    @JsonPropertyOrder({
        "partition",
        "lowWatermark"
    })
    @Reflectable
    public record TopicPartitionLowWatermark(
        @JsonProperty("partition")
        @JsonPropertyDescription("The topic partition.")
        int partition,

        @JsonProperty("lowWatermark")
        @JsonPropertyDescription("The low watermark for the topic partition.")
        long lowWatermark) {
    }
}
