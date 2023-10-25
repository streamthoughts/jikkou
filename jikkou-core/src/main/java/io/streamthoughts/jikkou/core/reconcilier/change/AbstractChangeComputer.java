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
package io.streamthoughts.jikkou.core.reconcilier.change;

import static io.streamthoughts.jikkou.core.reconcilier.ChangeType.ADD;
import static io.streamthoughts.jikkou.core.reconcilier.ChangeType.DELETE;
import static io.streamthoughts.jikkou.core.reconcilier.ChangeType.IGNORE;
import static io.streamthoughts.jikkou.core.reconcilier.ChangeType.NONE;
import static io.streamthoughts.jikkou.core.reconcilier.ChangeType.UPDATE;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import io.streamthoughts.jikkou.core.models.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.reconcilier.Change;
import io.streamthoughts.jikkou.core.reconcilier.ChangeComputer;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractChangeComputer<T, V, C extends Change> implements ChangeComputer<T, C> {

    private final ChangeKeyMapper<T> keyMapper;
    private final ChangeValueMapper<T, V> valueMapper;
    private boolean isDeleteOrphansEnabled;

    /**
     * Creates a new {@link AbstractChangeComputer} instance.
     *
     * @param isDeleteOrphansEnabled flag to indicate if orphans entries must be deleted.
     */
    public AbstractChangeComputer(final @NotNull ChangeKeyMapper<T> keyMapper,
                                  final @NotNull ChangeValueMapper<T, V> valueMapper,
                                  boolean isDeleteOrphansEnabled) {
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
        this.isDeleteOrphansEnabled = isDeleteOrphansEnabled;
    }

    /**
     * Sets whether orphaned entries should be deleted or ignored.
     *
     * @param isDeleteOrphansEnabled {@code true} to enable orphans deletion.
     */
    public void isDeleteOrphansEnabled(boolean isDeleteOrphansEnabled) {
        this.isDeleteOrphansEnabled = isDeleteOrphansEnabled;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<HasMetadataChange<C>> computeChanges(Iterable<T> actualStates,
                                                     Iterable<T> expectedStates) {

        Map<Object, T> actualStatesByKey = StreamSupport
                .stream(actualStates.spliterator(), false)
                .collect(Collectors.toMap(keyMapper::apply, it -> it));

        Map<Object, T> expectStatesByKey = StreamSupport
                .stream(expectedStates.spliterator(), false)
                .collect(Collectors.toMap(keyMapper::apply, it -> it));

        Map<ChangeType, List<BeforeAndAfter<V>>> groupedByChangeType = new HashMap<>();

        groupedByChangeType.putAll(StreamSupport
                .stream(expectedStates.spliterator(), false)
                .collect(Collectors
                        .groupingBy(after -> {
                                    Object key = keyMapper.apply(after);
                                    T before = actualStatesByKey.get(key);
                                    return getChangeType(before, after);
                                }
                                , mapping(after -> {
                                    Object key = keyMapper.apply(after);
                                    T before = actualStatesByKey.get(key);
                                    return newBeforeAndAfter(after, before);
                                }, toList())
                        )
                ));

        if (isDeleteOrphansEnabled) {
            List<BeforeAndAfter<V>> orphans = StreamSupport
                    .stream(actualStates.spliterator(), false)
                    .collect(Collectors
                            .groupingBy(before -> {
                                        Object key = keyMapper.apply(before);
                                        T after = expectStatesByKey.get(key);
                                        return after == null ? DELETE : IGNORE;
                                    }
                                    , mapping(before -> {
                                        Object key = keyMapper.apply(before);
                                        T after = expectStatesByKey.get(key);
                                        return newBeforeAndAfter(after, before);
                                    }, toList())
                            )
                    )
                    .getOrDefault(DELETE, Collections.emptyList());
            groupedByChangeType.computeIfAbsent(DELETE, type -> new ArrayList<>()).addAll(orphans);
        }

        // IGNORE are filtered and should not be part of the result changes
        return Stream.of(ADD, UPDATE, DELETE, NONE)
                .flatMap(changeType ->
                        groupedByChangeType.getOrDefault(changeType, Collections.emptyList())
                                .stream()
                                .flatMap(it -> handle(changeType, it).stream()
                                        .map(change -> GenericResourceChange
                                                .<C>builder()
                                                .withMetadata(it.meta)
                                                .withChange(change)
                                                .build()
                                        )
                                )
                ).collect(toList());
    }

    @NotNull
    private BeforeAndAfter<V> newBeforeAndAfter(T after, T before) {
        return new BeforeAndAfter<>(
                applyBeforeMapper(before),
                applyAfterMapper(before, after),
                getObjectMetadata(before, after)
        );
    }

    @Nullable
    private V applyBeforeMapper(T before) {
        return Optional.ofNullable(before).map(it -> valueMapper.apply(before, null)).orElse(null);
    }

    @Nullable
    private V applyAfterMapper(T before, T after) {
        return Optional.ofNullable(after).map(it -> valueMapper.apply(before, after)).orElse(null);
    }

    private List<C> handle(ChangeType type, BeforeAndAfter<V> object) {
        return switch (type) {
            case ADD -> buildChangeForCreating(object.after());
            case NONE -> buildChangeForNone(object.before(), object.after());
            case DELETE -> buildChangeForDeleting(object.before());
            case UPDATE -> buildChangeForUpdating(object.before(), object.after());
            case IGNORE -> null;
        };
    }

    protected abstract ObjectMeta getObjectMetadata(T before, T after);

    protected abstract ChangeType getChangeType(T before, T after);

    public abstract List<C> buildChangeForDeleting(V before);

    public abstract List<C> buildChangeForUpdating(V before, V after);

    public abstract List<C> buildChangeForNone(V before, V after);

    public abstract List<C> buildChangeForCreating(V after);

    private record BeforeAndAfter<T>(T before, T after, ObjectMeta meta) {
    }

    @FunctionalInterface
    public interface ChangeValueMapper<T, V> {

        /**
         * Maps a resource object into an object used for holding changes.
         *
         * @param before an actual resource object. Can be {@code null}.
         * @param after  an expected resource object. Can be {@code null}.
         * @return a grouping key
         */
        @NotNull V apply(@Nullable T before, @Nullable T after);
    }

    public static <T> ChangeValueMapper<T, T> identityChangeValueMapper() {
        return new IdentityChangeValueMapper<>();
    }

    static class IdentityChangeValueMapper<T> implements ChangeValueMapper<T, T> {

        @Override
        public @NotNull T apply(@Nullable T before, @Nullable T after) {
            if (after != null)
                return after;
            if (before != null) {
                return before;
            }
            throw new IllegalArgumentException("both arguments are null");
        }
    }

    @FunctionalInterface
    public interface ChangeKeyMapper<T> {

        /**
         * Computes a key used to join described resources (i.e. expected state)
         * with the existing one (i.e. actual state).
         *
         * @param object a resource object.
         * @return a change key.
         */
        @NotNull Object apply(@NotNull T object);
    }
}
