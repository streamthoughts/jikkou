/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import java.io.File;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GlobalConfigurationContextTest {

    // GIVEN
    static {
        URL resource = JikkouTest.class.getResource("/test-jikkou-config.json");
        String path = resource.getPath();
        GlobalConfigurationContext.setConfigurationContext(new ConfigurationContext(
                new File(path),
                new ObjectMapper()
        ));
    }

    @Test
    void shouldGetDefaultContext() {
        // When-Then
        Assertions.assertNotNull(GlobalConfigurationContext.getConfigurationContext());
    }

    @Test
    void shouldGetDefaultConfiguration() {
        // When-Then
        Assertions.assertNotNull(GlobalConfigurationContext.getConfiguration());
    }
}