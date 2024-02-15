/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime;

import static io.streamthoughts.jikkou.runtime.JikkouConfigProperties.EXTENSIONS_PROVIDER_DEFAULT_ENABLED;

import io.streamthoughts.jikkou.core.config.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JikkouConfigPropertiesTest {

    @Test
    void shouldGetExtensionProviderDefaultEnabled() {
        Configuration configFalse = Configuration.of(EXTENSIONS_PROVIDER_DEFAULT_ENABLED.key(), "false");
        Assertions.assertEquals(false, EXTENSIONS_PROVIDER_DEFAULT_ENABLED.get(configFalse));

        Configuration configTrue = Configuration.of(EXTENSIONS_PROVIDER_DEFAULT_ENABLED.key(), "true");
        Assertions.assertEquals(true, EXTENSIONS_PROVIDER_DEFAULT_ENABLED.get(configTrue));
    }

}