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
