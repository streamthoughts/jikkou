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

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Simple {@link Either} monad type.
 *
 * @param <L>   the {@link Left} type.
 * @param <R>   the {@link Right} type.
 */
public abstract class Either<L, R> {

    public static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    private final L left;
    private final R right;

    Either(final L left, final R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns {@code true} if this is a {@link Left}, {@code false} otherwise.
     */
    public abstract boolean isLeft();
    /**
     * Returns {@code true} if this is a {@link Right}, {@code false} otherwise.
     */
    public abstract boolean isRight();

    public LeftProjection<L, R> left() {
        return new LeftProjection<>(this);
    }

    public RightProjection<L, R> right() {
        return new RightProjection<>(this);
    }

    public <T> T fold(final Function<L, T> fl, final Function<R, T> fr) {
        return isLeft() ? fl.apply(left) : fr.apply(right);
    }

    public static class Left<L, R> extends Either<L, R> {

        private Left(L left) {
            super(left, null);
        }
        /**
         * @return {@code true}.
         */
        @Override
        public boolean isLeft() {
            return true;
        }
        /**
         * @return {@code false}.
         */
        @Override
        public boolean isRight() {
            return false;
        }
    }

    public static class Right<L, R> extends Either<L, R> {

        private Right(R right) {
            super(null, right);
        }

        /**
         * @return {@code false}.
         */
        @Override
        public boolean isLeft() {
            return false;
        }

        /**
         * @return {@code true}.
         */
        @Override
        public boolean isRight() {
            return true;
        }
    }

    public static class LeftProjection<L, R> {

        private final Either<L, R> either;

        LeftProjection(final Either<L, R> either) {
            Objects.requireNonNull(either, "either can't be null");
            this.either = either;
        }

        public boolean exists() {
            return either.isLeft();
        }

        public L get() {
            if (either.isLeft()) return either.left;
            else throw new NoSuchElementException("This is Right");
        }

        public <LL> Either<LL, R> map(final Function<? super L, ? extends LL> fn) {
            if (either.isLeft()) return Either.left(fn.apply(either.left));
            else return Either.right(either.right);
        }

        public <LL> Either<LL, R> flatMap(final Function<? super L, Either<LL, R>> fn) {
            if (either.isLeft()) return fn.apply(either.left);
            else return Either.right(either.right);
        }

        public Optional<L> toOptional() {
            return exists() ? Optional.of(either.left) : Optional.empty();
        }
    }

    public static class RightProjection<L, R> {

        private final Either<L, R> either;

        RightProjection(final Either<L, R> either) {
            Objects.requireNonNull(either, "either can't be null");
            this.either = either;
        }

        public boolean exists() {
            return either.isRight();
        }

        public R get() {
            if (either.isRight()) return either.right;
            else throw new NoSuchElementException("This is Left");
        }

        public <RR> Either<L, RR> map(final Function<? super R, ? extends RR> fn) {
            if (either.isRight()) return Either.right(fn.apply(either.right));
            else return Either.left(either.left);
        }

        public <RR> Either<L, RR> flatMap(final Function<? super R, Either<L, RR>> fn) {
            if (either.isRight()) return fn.apply(either.right);
            else return Either.left(either.left);
        }

        public Optional<R> toOptional() {
            return exists() ? Optional.of(either.right) : Optional.empty();
        }
    }
}