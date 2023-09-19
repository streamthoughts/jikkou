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
import io.streamthoughts.jikkou.kafka.internals.KafkaRecord;
import io.streamthoughts.jikkou.kafka.internals.KafkaUtils;
import io.streamthoughts.jikkou.kafka.internals.producer.DefaultProducerFactory;
import io.streamthoughts.jikkou.kafka.internals.producer.KafkaRecordSender;
import io.streamthoughts.jikkou.kafka.internals.producer.ProducerFactory;
import io.streamthoughts.jikkou.kafka.internals.producer.ProducerRequestResult;
import io.streamthoughts.jikkou.kafka.reporter.ce.CloudEventEntity;
import io.streamthoughts.jikkou.kafka.reporter.ce.CloudEventEntityBuilder;
import io.streamthoughts.jikkou.kafka.reporter.ce.CloudEventExtension;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.Producer;
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

    private KafkaChangeReporterConfig configuration;

    private ObjectMapper objectMapper = Jackson.JSON_OBJECT_MAPPER;

    private ProducerFactory<byte[], byte[]> producerFactory;

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
     * @param producerFactory the producer-client to be used for sending events.
     */
    public KafkaChangeReporter(final @NotNull ProducerFactory<byte[], byte[]> producerFactory,
                               final @NotNull ObjectMapper objectMapper) {
        this.producerFactory = Objects.requireNonNull(producerFactory, "producerFactory cannot be null");
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
        if (producerFactory == null) {
            producerFactory = new DefaultProducerFactory<>(
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
        List<KafkaRecord<byte[], byte[]>> records = stream.map(result -> {
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
                                .build();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        List<CompletableFuture<ProducerRequestResult<byte[], byte[]>>> futures;
        try (Producer<byte[], byte[]> producer = producerFactory.createProducer()) {
            futures = new KafkaRecordSender<>(producer).send(records);
            LOG.debug("Flushing any pending requests in producer");
            producer.flush();
        }
        try {
            AsyncUtils.waitForAll(futures).get();
            LOG.debug("Sending completed for {} records", results.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ignore) {
            // There is nothing we can do here
        }
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
    public void close() {
    }
}
