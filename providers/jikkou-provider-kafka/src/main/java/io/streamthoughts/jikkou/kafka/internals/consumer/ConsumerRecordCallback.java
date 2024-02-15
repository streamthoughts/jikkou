/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals.consumer;

import io.streamthoughts.jikkou.kafka.internals.KafkaRecord;
import java.util.function.Consumer;

/**
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public interface ConsumerRecordCallback<K, V> extends Consumer<KafkaRecord<K, V>> { }
