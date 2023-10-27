package io.streamthoughts.jikkou.core.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

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