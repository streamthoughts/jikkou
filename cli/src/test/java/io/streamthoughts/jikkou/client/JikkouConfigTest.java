/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client;

import io.streamthoughts.jikkou.runtime.JikkouConfig;
import io.streamthoughts.jikkou.runtime.JikkouConfigProperties;
import io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JikkouConfigTest {

    @Test
    void shouldLoadDefaultProviders() {
        // Given
        JikkouConfig config = JikkouConfig.load();

        // When
        List<ExtensionConfigEntry> providers = JikkouConfigProperties.PROVIDER_CONFIG.get(config);

        // Then
        Assertions.assertNotNull(providers);
        Assertions.assertFalse(providers.isEmpty());
    }
}