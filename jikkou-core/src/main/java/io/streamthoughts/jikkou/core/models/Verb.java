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

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public enum Verb {

    LIST("list"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    APPLY("apply");

    private final String value;

    Verb(String value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public String value() {
        return value;
    }

    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return value;
    }

    public static Verb[] getForNamesIgnoreCase(final @Nullable List<String> verbs) {
        if (verbs == null) return new Verb[]{};
        if (verbs.size() == 1 && verbs.get(0).equalsIgnoreCase("*")) {
            return Verb.values();
        }
        return verbs.stream()
                .map(str -> Arrays.stream(Verb.values())
                        .filter(e -> e.name().equals(str.toUpperCase(Locale.ROOT)))
                        .findFirst()
                        .orElseThrow(() -> new JikkouRuntimeException("Unsupported verb '" + str + "'"))
        ).toArray(Verb[]::new);
    }

}
