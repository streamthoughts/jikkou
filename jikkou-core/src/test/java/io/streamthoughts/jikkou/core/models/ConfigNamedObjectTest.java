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
package io.streamthoughts.jikkou.core.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigNamedObjectTest {

    static final boolean NO_MEANINGFUL_BOOLEAN = false;
    static final String NO_MEANINGFUL_STRING = "???";

    @Test
    void should_return_default_value() {
        ConfigValue defaultValue = new ConfigValue(NO_MEANINGFUL_STRING, NO_MEANINGFUL_STRING);
        Assertions.assertTrue(defaultValue.isDeletable());
        Assertions.assertFalse(defaultValue.isDefault());
    }

    @Test
    void should_return_passed_deletable() {
        ConfigValue valTrue = new ConfigValue(NO_MEANINGFUL_STRING, NO_MEANINGFUL_STRING, NO_MEANINGFUL_BOOLEAN, true);
        Assertions.assertTrue(valTrue.isDeletable());
        ConfigValue valFalse = new ConfigValue(NO_MEANINGFUL_STRING, NO_MEANINGFUL_STRING, NO_MEANINGFUL_BOOLEAN, false);
        Assertions.assertFalse(valFalse.isDeletable());
    }

    @Test
    void should_return_passed_default() {
        ConfigValue valTrue = new ConfigValue(NO_MEANINGFUL_STRING, NO_MEANINGFUL_STRING, true, NO_MEANINGFUL_BOOLEAN);
        Assertions.assertTrue(valTrue.isDefault());
        ConfigValue valFalse = new ConfigValue(NO_MEANINGFUL_STRING, NO_MEANINGFUL_STRING, false, NO_MEANINGFUL_BOOLEAN);
        Assertions.assertFalse(valFalse.isDefault());
    }

}