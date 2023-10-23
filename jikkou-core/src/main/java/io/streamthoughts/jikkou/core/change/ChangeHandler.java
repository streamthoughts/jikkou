/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.core.change;

import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to apply a changes into the managed system.
 *
 * @param <T> type of the change.
 */
public interface ChangeHandler<T extends Change> {

    /**
     * Checks whether this handler can accept the given change.
     *
     * @return {@code true} if the {@code change} is accepted. Otherwise {@code false}.
     */
    Set<ChangeType> supportedChangeTypes();

    /**
     * Executes this handler for the given list of changes.
     *
     * @param items the list of objects holding a {@link Change}.
     * @return the list of change application response.
     */

    List<ChangeResponse<T>> apply(@NotNull final List<HasMetadataChange<T>> items);

    /**
     * Returns a textual description for the given {@link Change}.
     *
     * @param item an object holding a {@link Change}.
     * @return the textual description for this item.
     */
    ChangeDescription getDescriptionFor(@NotNull final HasMetadataChange<T> item);

    /**
     * Static helper method to verify that a given handler do support a specific change.
     *
     * @param handler the handler to verify.
     * @param item   the change to verify.
     * @param <T>     type of the change.
     * @throws IllegalArgumentException if the change is not support.
     */
    static <T extends Change> void verify(final ChangeHandler<T> handler,
                                          final @NotNull HasMetadataChange<?> item) {
        Set<ChangeType> supportedChangeTypes = handler.supportedChangeTypes();
        if (!supportedChangeTypes.contains(item.getChange().operation())) {
            throw new IllegalArgumentException(
                    String.format(
                            "'%s' class does not support the passed change: %s",
                            handler.getClass().getName(), item
                    )
            );
        }
    }

    class None<T extends Change> implements ChangeHandler<T> {

        private final Function<HasMetadataChange<T>, ChangeDescription> description;

        public None(Function<HasMetadataChange<T>, ChangeDescription> description) {
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
        public List<ChangeResponse<T>> apply(@NotNull List<HasMetadataChange<T>> changes) {
            return changes.stream().map(ChangeResponse::new).collect(Collectors.toList());
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public ChangeDescription getDescriptionFor(@NotNull HasMetadataChange<T> item) {
            return description.apply(item);
        }
    }
}
