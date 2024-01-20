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
