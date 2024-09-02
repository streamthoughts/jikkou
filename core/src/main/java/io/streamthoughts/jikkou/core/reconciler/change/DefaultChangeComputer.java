/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler.change;

import static java.util.stream.Collectors.toList;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.reconciler.change.ChangeComputerBuilder.ChangeFactory;
import io.streamthoughts.jikkou.core.reconciler.change.ChangeComputerBuilder.KeyMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link DefaultChangeComputer}.
 *
 * @param <K> The type of the state key.
 * @param <V> The type of the state value.
 * @param <R> The type of the state change.
 */
public final class DefaultChangeComputer<K, V, R> implements ChangeComputer<V, R> {

    private final boolean isDeleteOrphans;

    private final KeyMapper<V, K> keyMapper;

    private final ChangeFactory<K, V, R> changeFactory;

    /**
     * Creates a new {@link DefaultChangeComputer} instance.
     *
     * @param isDeleteOrphans Specifies whether orphans must be deleted.
     * @param keyMapper       The {@link KeyMapper}.
     * @param changeFactory   The {@link ChangeFactory}.
     */
    public DefaultChangeComputer(boolean isDeleteOrphans,
                                 @NotNull ChangeComputerBuilder.KeyMapper<V, K> keyMapper,
                                 @NotNull ChangeComputerBuilder.ChangeFactory<K, V, R> changeFactory) {
        this.isDeleteOrphans = isDeleteOrphans;
        this.keyMapper = keyMapper;
        this.changeFactory = changeFactory;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<R> computeChanges(Iterable<V> actualStates,
                                  Iterable<V> expectedStates) {
        // Group all actualStates by resource ID.
        Map<K, V> actualStatesByID = groupById(actualStates);

        // Group all expectedStates by resource ID.
        Map<K, V> expectStatesByID = groupById(expectedStates);

        return computeChanges(actualStatesByID, expectStatesByID);
    }

    @NotNull
    private Map<K, V> groupById(final Iterable<V> actualStates) {
        if (actualStates == null) {
            return Collections.emptyMap();
        }
        return StreamSupport
                .stream(actualStates.spliterator(), false)
                .collect(Collectors.toMap(keyMapper::apply, Function.identity(), this::duplicateKeyException, LinkedHashMap::new));
    }

    public List<R> computeChanges(final Map<K, V> actualStatesByID,
                                  final Map<K, V> expectStatesByID) {

        int maxTotalStates = actualStatesByID.size() + expectStatesByID.size();
        List<BeforeAndAfter<K, V>> joined = new ArrayList<>(maxTotalStates);

        joined.addAll(rightJoin(actualStatesByID, expectStatesByID));

        if (isDeleteOrphans) {
            joined.addAll(leftJoinWhereRightIsNull(actualStatesByID, expectStatesByID));
        }

        return joined.stream().flatMap(this::createChange).collect(toList());
    }

    @NotNull
    private Stream<? extends R> createChange(final BeforeAndAfter<K, V> states) {
        return changeFactory.createChange(states.key(), states.before(), states.after()).stream();
    }

    @NotNull
    private List<BeforeAndAfter<K, V>> leftJoinWhereRightIsNull(final Map<K, V> leftStatesByID,
                                                                final Map<K, V> rightStatesByID) {
        return leftStatesByID.entrySet()
                .stream()
                .map(left -> {
                    K id = left.getKey();
                    return new BeforeAndAfter<>(id, left.getValue(), rightStatesByID.get(id));
                })
                .filter(BeforeAndAfter::isAfterNull)
                .toList();
    }


    @NotNull
    private List<BeforeAndAfter<K, V>> rightJoin(final Map<K, V> leftStatesByID,
                                                 final Map<K, V> rightStatesByID) {
        return rightStatesByID.entrySet()
                .stream()
                .map(right -> {
                    K id = right.getKey();
                    V leftValue = leftStatesByID.get(id); // Can be null
                    return new BeforeAndAfter<>(id, leftValue, right.getValue());
                })
                .toList();
    }

    @NotNull
    private V duplicateKeyException(V o1, V o2) {
        K key = keyMapper.apply(o1);
        throw new JikkouRuntimeException("duplicate state key '" + key + "'");
    }
}
