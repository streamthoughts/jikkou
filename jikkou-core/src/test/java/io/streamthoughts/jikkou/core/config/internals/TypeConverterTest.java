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
package io.streamthoughts.jikkou.core.config.internals;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TypeConverterTest {

    @Test
    void testConvertToListGivenList() {
        List result = TypeConverter.getList(List.of("foo"), true);
        Assertions.assertEquals(List.of("foo"), result);
    }

    @Test
    void testConvertToListGivenArray() {
        List result = TypeConverter.getList(new String[]{"foo"}, true);
        Assertions.assertEquals(List.of("foo"), result);
    }

    @Test
    void testConvertToListGivenString() {
        List result = TypeConverter.getList("foo", true);
        Assertions.assertEquals(List.of("foo"), result);
    }

    @Test
    void testConvertToBoolGivenTrue() {
        Assertions.assertTrue(TypeConverter.getBool("true"));
    }

    @Test
    void testConvertToBoolGivenYes() {
        Assertions.assertTrue(TypeConverter.getBool("yes"));
    }

    @Test
    void testConvertToBoolGivenFalse() {
        Assertions.assertFalse(TypeConverter.getBool("false"));
    }

    @Test
    void testConvertToBoolGivenNo() {
        Assertions.assertFalse(TypeConverter.getBool("no"));
    }
}
