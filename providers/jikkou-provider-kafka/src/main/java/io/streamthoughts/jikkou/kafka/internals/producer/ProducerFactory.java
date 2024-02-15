/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals.producer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

public interface ProducerFactory<K, V> extends AutoCloseable {

    /**
     * Creates a new Producer instance.
     *
     * @return  a new {@link Consumer} instance.
     */
    Producer<K, V> createProducer();

    /**
     * Close this factory.
     */
    @Override
    void close();
}
