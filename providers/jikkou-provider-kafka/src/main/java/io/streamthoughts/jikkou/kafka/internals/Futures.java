/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals;

import java.util.concurrent.CompletableFuture;
import org.apache.kafka.common.KafkaFuture;

/**
 * Utility method to manipulate {@link KafkaFuture}.
 */
public final class Futures {

    private Futures() {}

    public static <T> CompletableFuture<T> toCompletableFuture(final KafkaFuture<T> future) {
        return future.toCompletionStage().toCompletableFuture();
    }

}
