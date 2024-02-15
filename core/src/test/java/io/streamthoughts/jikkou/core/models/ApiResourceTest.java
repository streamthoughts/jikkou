/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApiResourceTest {

    @Test
    void shouldReturnTrueForSupportedVerb() {
        ApiResource resource = new ApiResource(
                "name",
                "kind",
                "singularName",
                Collections.emptySet(),
                "description",
                Set.of("list")
        );
        Assertions.assertTrue(resource.isVerbSupported(Verb.LIST));
    }

    @Test
    void shouldReturnTrueForNotSupportedVerb() {
        ApiResource resource = new ApiResource(
                "name",
                "kind",
                "singularName",
                Collections.emptySet(),
                "description",
                Set.of("list")
        );
        Assertions.assertFalse(resource.isVerbSupported(Verb.CREATE));
    }

    @Test
    void shouldReturnVerbOptionListForSupportedVerb() {
        ApiResource resource = new ApiResource(
                "name",
                "kind",
                "singularName",
                Collections.emptySet(),
                "description",
                Set.of("list")
        );
        resource = resource.withApiResourceVerbOptionList(
                new ApiResourceVerbOptionList(Verb.LIST, Collections.emptyList())
        );
        Assertions.assertTrue(resource.getVerbOptionList(Verb.LIST).isPresent());
    }
}