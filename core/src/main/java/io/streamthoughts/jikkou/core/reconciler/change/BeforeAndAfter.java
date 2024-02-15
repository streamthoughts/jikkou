/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler.change;

/**
 * Wraps a state before/after an operation.
 *
 * @param key    The state key.
 * @param before The state before the operation.
 * @param after  The state after the operation.
 * @param <V>    The type of the state.
 */
record BeforeAndAfter<K, V>(K key, V before, V after) {

    public boolean isBeforeNull() {
        return before == null;
    }

    public boolean isAfterNull() {
        return after == null;
    }
}
