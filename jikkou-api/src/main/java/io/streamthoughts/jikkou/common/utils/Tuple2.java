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
package io.streamthoughts.jikkou.common.utils;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * A immutable tuple of two elements.
 *
 * @param <T1> the type of the 1st element.
 * @param <T2> the type of the 2nd element.
 */
public final class Tuple2<T1, T2> {

    public static <T1, T2> Tuple2<T1, T2> of(Map.Entry<T1, T2> entry) {
        return new Tuple2<>(entry.getKey(), entry.getValue());
    }

    public static <T1, T2> Tuple2<T1, T2> of(T1 o1, T2 o2) {
        return new Tuple2<>(o1, o2);
    }

    private final T1 _1;
    private final T2 _2;

    /**
     * Creates a tuple of two elements.
     *
     * @param _1 the 1st element
     * @param _2 the 2nd element
     */
    public Tuple2(T1 _1, T2 _2) {
        this._1 = _1;
        this._2 = _2;
    }

    /**
     * Gets the 1st element.
     *
     * @return the value, or {@code null}.
     */
    public T1 _1() {
        return _1;
    }

    /**
     * Gets the 2st element.
     *
     * @return the value, or {@code null}.
     */
    public T2 _2() {
        return _2;
    }

    public <R> Tuple2<R, T2> mapLeft(Function<? super T1, ? extends R> mapper) {
        return new Tuple2<>(mapper.apply(_1), _2);
    }

    public <R> Tuple2<T1, R> mapRight(Function<? super T2, ? extends R> mapper) {
        return new Tuple2<>(_1, mapper.apply(_2));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return Objects.equals(_1, tuple2._1) && Objects.equals(_2, tuple2._2);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(_1, _2);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "(" + _1 + "," + _2 + ')';
    }
}
