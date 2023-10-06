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
package io.streamthoughts.jikkou.api.model;

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