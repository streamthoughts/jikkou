/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.common.utils.Enums;
import java.util.Collection;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public enum Verb {

    LIST("list"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    GET("get"),
    APPLY("apply");

    private final String value;

    Verb(String value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public String value() {
        return value;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return value;
    }

    public static Verb getForNameIgnoreCase(final @Nullable String verb) {
        return Enums.getForNameIgnoreCase(verb, Verb.class);
    }

    public static Verb[] getForNamesIgnoreCase(final @Nullable Collection<String> verbs) {
        if (verbs == null) return new Verb[]{};
        if (verbs.size() == 1 && verbs.iterator().next().equalsIgnoreCase("*")) {
            return Verb.values();
        }
        return verbs.stream()
                .map(Verb::getForNameIgnoreCase)
                .toArray(Verb[]::new);
    }

}
