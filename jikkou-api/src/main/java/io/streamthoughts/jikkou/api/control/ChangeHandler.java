/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.api.control;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to apply a changes into the managed system.
 *
 * @param <T> type of the change.
 */
public interface ChangeHandler<T extends Change> extends
        Function<List<T>, List<ChangeResponse<T>>> {

    /**
     * Checks whether this handler can accept the given change.
     *
     * @return {@code true} if the {@code change} is accepted. Otherwise {@code false}.
     */
    Set<ChangeType> supportedChangeTypes();

    /**
     * Executes this handler for the given list of changes.
     *
     * @param changes the list of change to be applied.
     * @return a map of handler results.
     */
    @Override
    List<ChangeResponse<T>> apply(@NotNull final List<T> changes);

    /**
     * Returns a textual description for the given {@link Change}.
     *
     * @param change the {@link Change}.
     * @return the textual description for this change.
     */
    ChangeDescription getDescriptionFor(@NotNull final T change);

    /**
     * Static helper method to verify that a given handler do support a specific change.
     *
     * @param handler the handler to verify.
     * @param change  the change to verify.
     * @param <T>     type of the change.
     * @throws IllegalArgumentException if the change is not support.
     */
    static <T extends Change> void verify(final ChangeHandler<T> handler,
                                          final @NotNull Change change) {
        Set<ChangeType> supportedChangeTypes = handler.supportedChangeTypes();
        if (!supportedChangeTypes.contains(change.getChangeType())) {
            throw new IllegalArgumentException(
                    String.format(
                            "'%s' class does not support the passed change: %s",
                            handler.getClass().getName(), change
                    )
            );
        }
    }

    class None<T extends Change> implements ChangeHandler<T> {

        private final Function<T, ChangeDescription> description;

        public None(Function<T, ChangeDescription> description) {
            this.description = description;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public Set<ChangeType> supportedChangeTypes() {
            return Set.of(ChangeType.NONE);
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public List<ChangeResponse<T>> apply(@NotNull List<T> changes) {
            return Collections.emptyList();
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public ChangeDescription getDescriptionFor(@NotNull T change) {
            return description.apply(change);
        }
    }
}
