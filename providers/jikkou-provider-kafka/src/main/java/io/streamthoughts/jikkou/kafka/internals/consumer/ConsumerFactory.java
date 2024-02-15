/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals.consumer;

import org.apache.kafka.clients.consumer.Consumer;

/**
 * Interface to create a Consumer instance.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public interface ConsumerFactory<K, V>  {

    /**
     * Creates a new Consumer instance.
     *
     * @return  a new {@link Consumer} instance.
     */
    Consumer<K, V> createConsumer();

    /**
     * Creates a new Consumer instance with an explicit {@code client.id}.
     *
     * @param clientId - override the {@code client.id} property.
     *
     * @return  a new {@link Consumer} instance.
     */
    Consumer<K, V> createConsumer(String clientId);

}
