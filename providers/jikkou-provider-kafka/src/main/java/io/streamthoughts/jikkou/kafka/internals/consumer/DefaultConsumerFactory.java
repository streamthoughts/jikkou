/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals.consumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default factory to create Consumer instances.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public class DefaultConsumerFactory<K, V> implements ConsumerFactory<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultConsumerFactory.class);

    private final Map<String, Object> clientProperties;
    private Deserializer<K> keyDeserializer = null;
    private Deserializer<V> valueDeserializer = null;

    public DefaultConsumerFactory(@NotNull Map<String, Object> clientProperties) {
        this(clientProperties, null, null);
    }

    public DefaultConsumerFactory<K, V> setKeyDeserializer(Deserializer<K> keyDeserializer) {
        this.keyDeserializer = keyDeserializer;
        return this;
    }

    public DefaultConsumerFactory<K, V> setValueDeserializer(Deserializer<V> valueDeserializer) {
        this.valueDeserializer = valueDeserializer;
        return this;
    }

    public DefaultConsumerFactory(@NotNull Map<String, Object> clientProperties,
                                  @Nullable Deserializer<K> keyDeserializer,
                                  @Nullable Deserializer<V> valueDeserializer) {
        this.clientProperties = Collections.unmodifiableMap(clientProperties);
        this.keyDeserializer = keyDeserializer;
        this.valueDeserializer = valueDeserializer;
    }

    /** {@inheritDoc} **/
    @Override
    public Consumer<K, V> createConsumer() {
        LOG.debug("Creating consumer");
        return new KafkaConsumer<>(clientProperties, keyDeserializer, valueDeserializer);
    }


    /** {@inheritDoc} **/
    @Override
    public Consumer<K, V> createConsumer(String clientId) {
        LOG.debug("Creating consumer with client.id={}", clientId);
        Map<String, Object> props = new HashMap<>(clientProperties);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        return new KafkaConsumer<>(props, keyDeserializer, valueDeserializer);
    }
}
