/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class ConfigTest {

    @Test
    void shouldCreateClientFromUrl() {
        Assertions.assertNotNull(
                Config.fromUrl("http://localhost:8080"));
    }

    @Test
    void shouldCreateClientFromUserPassword() {
        Assertions.assertNotNull(
                Config.fromUserPassword("http://localhost:8080", "admin", "admin", false));
    }
}