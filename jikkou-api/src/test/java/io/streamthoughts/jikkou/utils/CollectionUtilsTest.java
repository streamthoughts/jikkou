/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.utils;

import io.streamthoughts.jikkou.common.utils.CollectionUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CollectionUtilsTest {

    @Test
    public void test_should_return_nested_map() {

        Map<String, Object> result = new HashMap<>();

        CollectionUtils.toNestedMap(
                Map.of(
                "key1.key2.key3", "value1",
                "key1.key2.key4", "value2"
                ),
                result,
                null
        );

        Assertions.assertNotNull(getValue("key1", result));
        Assertions.assertNotNull(getValue("key1.key2", result));
        Assertions.assertEquals("value1", getValue("key1.key2.key3", result).toString());
        Assertions.assertEquals("value2", getValue("key1.key2.key4", result).toString());
    }

    @Test
    public void test_should_fail_return_nested_map_given_duplicate_key() {
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            Map<String, Object> result = new HashMap<>();

            CollectionUtils.toNestedMap(
                    Map.of(
                            "key1.key2", "value1",
                            "key1.key2.key3", "value2"
                    ),
                    result,
                    null
            );
        });
    }


    static Object getValue(final String path, Map<String, Object> mapValues) {
        List<String> paths = List.of(path.split("\\."));
        Object result = null;
        Iterator<String> iterator = paths.iterator();
        while(iterator.hasNext()) {
            String key = iterator.next();
            if (!iterator.hasNext()) {
                result =  mapValues.get(key);
                break;
            }

            if (mapValues.containsKey(key)) {
                mapValues = (Map<String, Object>) mapValues.get(key);
            } else {
                break;
            }
        }
        return result;
    }
}