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
package io.streamthoughts.jikkou.kafka.reporter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.annotation.ExtensionEnabled;
import io.streamthoughts.jikkou.api.JikkouInfo;
import io.streamthoughts.jikkou.api.change.Change;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.io.Jackson;
import io.streamthoughts.jikkou.api.reporter.ChangeReporter;
import io.streamthoughts.jikkou.common.utils.AsyncUtils;
import io.streamthoughts.jikkou.common.utils.CompletablePromise;
import io.streamthoughts.jikkou.common.utils.CompletablePromiseContext;
import io.streamthoughts.jikkou.kafka.internals.KafkaUtils;
import io.streamthoughts.jikkou.kafka.reporter.ce.CloudEventEntity;
import io.streamthoughts.jikkou.kafka.reporter.ce.CloudEventEntityBuilder;
import io.streamthoughts.jikkou.kafka.reporter.ce.CloudEventExtension;
import io.streamthoughts.jikkou.kafka.reporter.ce.KafkaRecord;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This reporter can be used to send change results into a kafka topic as Cloud Event.
 */
@ExtensionEnabled(value = false)
public class KafkaChangeReporter implements ChangeReporter {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaChangeReporter.class);
    public static final int DEFAULT_KAFKA_TIMEOUT = 30;

    private KafkaChangeReporterConfig configuration;

    private ObjectMapper objectMapper = Jackson.JSON_OBJECT_MAPPER;

    private Supplier<Producer<byte[], byte[]>> producerSupplier;

    /**
     * Creates a new {@link KafkaChangeReporter} instance.
     */
    public KafkaChangeReporter() {
        super();
    }

    /**
     * Creates a new {@link KafkaChangeReporter} instance.
     *
     * @param configuration the application's configuration.
     */
    public KafkaChangeReporter(final @NotNull Configuration configuration) {
        configure(configuration);
    }

    /**
     * Creates a new {@link KafkaChangeReporter} instance.
     *
     * @param producerSupplier the producer-client to be used for sending events.
     */
    public KafkaChangeReporter(final @NotNull Supplier<Producer<byte[], byte[]>> producerSupplier,
                               final @NotNull ObjectMapper objectMapper) {
        this.producerSupplier = Objects.requireNonNull(producerSupplier, "producerSupplier cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        LOG.info("Configuration");
        this.configuration = new KafkaChangeReporterConfig(configuration);
        Map<String, Object> clientConfig = this.configuration.clientConfig();
        if (producerSupplier == null) {
            producerSupplier = () -> new KafkaProducer<>(
                    KafkaUtils.getProducerClientConfigs(clientConfig),
                    new ByteArraySerializer(),
                    new ByteArraySerializer()
            );
        }

        if (this.configuration.isTopicCreationEnabled()) {
            try (var adminClient = AdminClient.create(KafkaUtils.getAdminClientConfigs(clientConfig))) {
                createTopics(
                        adminClient,
                        this.configuration.topicName(),
                        (short) this.configuration.defaultReplicationFactor()
                );
            }
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void report(List<ChangeResult<Change>> results) {
        LOG.info("Starting reporting for {} changes", results.size());
        final String topic = configuration.topicName();
        final String source = configuration.eventSource();

        Stream<ChangeResult<Change>> stream = filterRelevantChangeResults(results);
        List<ProducerRecord<byte[], byte[]>> records = stream.map(result -> {
                    CloudEventEntity<Object> entity = CloudEventEntityBuilder.newBuilder()
                            .withSpecVersion("1.0")
                            .withId("uuid:" + UUID.randomUUID())
                            .withTime(ZonedDateTime.now(ZoneOffset.UTC))
                            .withType("io.jikkou.resourcechangeevent")
                            .withSource(source)
                            .withDataContentType("application/json")
                            .withExtension(CloudEventExtension.of("iojikkouversion", JikkouInfo.getVersion()))
                            .withData(result)
                            .build();
                    try {
                        byte[] value = objectMapper.writeValueAsBytes(entity);
                        return KafkaRecord.<byte[], byte[]>builder()
                                .header("content-type", "application/cloudevents+json; charset=UTF-8")
                                .value(value)
                                .topic(topic)
                                .build()
                                .toProducerRecord();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
        try (var producer = producerSupplier.get()) {
            try (CompletablePromiseContext context = CompletablePromiseContext.eventLoop()) {
                List<CompletableFuture<Void>> futures = records
                        .stream()
                        .map(r -> this.send(producer, r, context))
                        .toList();
                CompletableFuture<List<Void>> all = AsyncUtils.waitForAll(futures);
                try {
                    producer.flush();
                    all.get(DEFAULT_KAFKA_TIMEOUT, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception ex) {
                    // ignore - there is nothing useful we can do here
                } finally {
                    LOG.info("Completed reporting for {} change events", futures.size());
                }
            }
        }
    }

    private CompletableFuture<Void> send(final Producer<byte[], byte[]> producer,
                                         final ProducerRecord<byte[], byte[]> record,
                                         final CompletablePromiseContext promiseContext) {
        return new CompletablePromise<>(producer.send(record), promiseContext)
                .handle((metadata, e) -> {
                    if (e == null) {
                        LOG.info(
                                "Changed event was successfully sent to kafka topic {}-{} ",
                                metadata.topic(),
                                metadata.partition()
                        );
                    } else {
                        LOG.warn("Failed to report change event into kafka topic {}",
                                metadata.topic(),
                                e);
                    }
                    return null;
                });
    }

    private Stream<ChangeResult<Change>> filterRelevantChangeResults(List<ChangeResult<Change>> results) {
        return results.stream().filter(it -> it.isChanged() && !it.isFailed());
    }

    private void createTopics(final AdminClient client,
                              final String topic,
                              final short replicas) {
        LOG.info("Creating reporting topic: {}", topic);
        try {
            client.createTopics(List.of(new NewTopic(topic, 1, replicas))).all().get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause != null & cause instanceof TopicExistsException) {
                LOG.info("Cannot auto create topic {} - topics already exists. Error can be ignored.", topic);
            } else {
                LOG.error("Cannot auto create topic {}", topic, e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // ignore and attempts to proceed anyway;
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void close() {}
}
