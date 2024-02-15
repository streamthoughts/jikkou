/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ObjectMetaTest {

    @Test
    void shouldBuildObjectMeta() {
        ObjectMeta actual = ObjectMeta.builder()
                .withName("name")
                .withLabel("l1", "v1")
                .withLabels(Map.of("l2", "v2"))
                .withAnnotation("a1", "v1")
                .withAnnotations(Map.of("a2", "v2"))
                .build();

        Assertions.assertEquals(new ObjectMeta(
                "name",
                Map.of("l1", "v1", "l2", "v2"),
                Map.of("a1", "v1", "a2", "v2")
        ), actual);
    }

    @Test
    void shouldFindAnnotationByKey() {
        ObjectMeta meta = new ObjectMeta(
                "name",
                Collections.emptyMap(),
                Map.of("k", "v")
        );
        Assertions.assertTrue(meta.findAnnotationByKey("k").isPresent());
        Assertions.assertFalse(meta.findAnnotationByKey("dummy").isPresent());
    }

    @Test
    void shouldAddAnnotationIfAbsent() {
        ObjectMeta meta = new ObjectMeta(
                "name",
                Collections.emptyMap(),
                Map.of("k1", "v1")
        );

        meta.addAnnotationIfAbsent("k1", "v2");
        meta.addAnnotationIfAbsent("k2", "v1");
        Assertions.assertEquals("v1", meta.findAnnotationByKey("k1").get());
        Assertions.assertEquals("v1", meta.findAnnotationByKey("k2").get());
    }

}