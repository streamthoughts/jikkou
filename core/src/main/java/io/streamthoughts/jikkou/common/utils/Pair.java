/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A immutable tuple of two elements.
 *
 * @param <T1> the type of the 1st element.
 * @param <T2> the type of the 2nd element.
 */
public record Pair<T1, T2>(T1 _1, T2 _2) {

    public static <T1, T2> Pair<T1, T2> of(Map.Entry<T1, T2> entry) {
        return new Pair<>(entry.getKey(), entry.getValue());
    }

    public static <T1, T2> Pair<T1, T2> of(T1 o1, T2 o2) {
        return new Pair<>(o1, o2);
    }

    /**
     * Creates a tuple of two elements.
     *
     * @param _1 the 1st element
     * @param _2 the 2nd element
     */
    public Pair {
    }

    public <R> Pair<R, T2> mapLeft(Function<? super T1, ? extends R> mapper) {
        return new Pair<>(mapper.apply(_1), _2);
    }

    public <R> Pair<T1, R> mapRight(Function<? super T2, ? extends R> mapper) {
        return new Pair<>(_1, mapper.apply(_2));
    }

    public Stream<Pair<T1, T2>> stream() {
        return Stream.of(this);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "(" + _1 + "," + _2 + ')';
    }
}
