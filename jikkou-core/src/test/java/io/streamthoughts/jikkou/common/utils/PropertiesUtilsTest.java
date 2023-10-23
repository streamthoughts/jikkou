/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.common.utils;

import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PropertiesUtilsTest {

    @Test
    void should_convert_map_to_properties() {
        var properties = PropertiesUtils.fromMap(Map.of("k1", "v1", "k2", "v2"));
        Assertions.assertEquals(2, properties.size());
        Assertions.assertEquals("v1", properties.get("k1"));
        Assertions.assertEquals("v2", properties.get("k2"));
    }

    @Test
    void should_convert_properties_to_map() {
        var props = new Properties();
        props.put("k1", "v1");
        props.put("k2", "v2");
        var properties = PropertiesUtils.toMap(props);
        Assertions.assertEquals(2, properties.size());
        Assertions.assertEquals("v1", properties.get("k1"));
        Assertions.assertEquals("v2", properties.get("k2"));
    }
}