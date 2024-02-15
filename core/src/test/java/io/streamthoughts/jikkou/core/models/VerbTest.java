/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VerbTest {

    public static final String WILDCARD = "*";

    @Test
    void shouldGetAllVerbsGivenWildcard() {
        Verb[] verbs = Verb.getForNamesIgnoreCase(List.of(WILDCARD));
        Assertions.assertArrayEquals(Verb.values(), verbs);
    }

    @Test
    void shouldGetVerbGivenSingleString() {
        Verb[] verbs = Verb.getForNamesIgnoreCase(List.of("list"));
        Assertions.assertArrayEquals(new Verb[]{Verb.LIST}, verbs);
    }
}