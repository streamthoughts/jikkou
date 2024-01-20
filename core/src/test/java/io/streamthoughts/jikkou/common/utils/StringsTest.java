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
package io.streamthoughts.jikkou.common.utils;

import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringsTest {

    @Test
    void shouldReturnTrueForNullString() {
        Assertions.assertTrue(Strings.isBlank(null));
    }

    @Test
    void shouldReturnTrueForEmptyString() {
        Assertions.assertTrue(Strings.isBlank(""));
    }

    @Test
    void shouldReturnTrueForBlankString() {
        Assertions.assertTrue(Strings.isBlank("     "));
    }

    @Test
    void shouldReturnFalseForNonEmptyString() {
        Assertions.assertFalse(Strings.isBlank("dummy"));
    }

    @Test
    void shouldGetPropertiesWhenParsingValidString() {
        Properties properties = Strings.toProperties("a=1,b=2,c=3");
        Assertions.assertEquals(new Properties(){{
            setProperty("a", "1");
            setProperty("b", "2");
            setProperty("c", "3");
        }}, properties);
    }
    
    @Test
    void shouldPruneSuffixedString() {
        Assertions.assertEquals("string", Strings.pruneSuffix( "string-suffix", "-suffix"));
    }
}