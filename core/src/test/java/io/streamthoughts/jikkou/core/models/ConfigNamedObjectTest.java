/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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