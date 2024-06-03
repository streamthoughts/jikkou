/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler.change;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Functional interface for comparing equality between two objects.
 *
 * @param <T> type of the object to be compared.
 */
public interface StateComparator<T> {

    /**
     * Returns {@code true} if the two given objects are equal.
     *
     * @param before The before state object - can't be null.
     * @param after  The after state object - can't be null.
     *
     * @return {@code true} if the state are equal. Otherwise {@code false.}
     */
    boolean equals(@NotNull T before, @NotNull T after);

    static <T> StateComparator<T> True() {
        return (before, after) -> true;
    }

    static <T> StateComparator<T> Equals() {
      return (before, after) -> {
          if (after instanceof String)
              return Objects.equals(after, before.toString());

          if (before instanceof String)
              return Objects.equals(before, after.toString());

          return Objects.equals(before, after);
      };
    }
}
