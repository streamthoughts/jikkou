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
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * Represents a list of items of type {@link T}.
 *
 * @param <T>   the type of items in the list.
 */
@InterfaceStability.Evolving
public interface Listeable<T> extends Iterable<T> {

    /**
     * Gets the items.
     *
     * @return the items.
     */
    List<T> getItems();

    /**
     * @return {@code true} if this list contains no items.
     */
    @JsonIgnore
    default boolean isEmpty() {
        return getItems().isEmpty();
    }

    /**
     * @return  a sequential {@link Stream} with this list as its source.
     */
    default Stream<T> stream() {
        return getItems().stream();
    }

    /**
     * @return the first item in this list.
     * @throws NoSuchElementException if this list contains no items.
     */
    default T first() {
        if (isEmpty()) {
            throw new NoSuchElementException("Items list is empty");
        }
        return getItems().get(0);
    }

    /**
     * @return the number of items in this list.
     */
    default int size() {
        return getItems().size();
    }

    /** {@inheritDoc} */
    @Override
    default Iterator<T> iterator() {
        return getItems().iterator();
    }
}
