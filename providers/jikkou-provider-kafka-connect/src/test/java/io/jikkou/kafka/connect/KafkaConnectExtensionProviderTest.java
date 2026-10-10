/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.connect;

import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.ConfigException;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaConnectExtensionProviderTest {

    @Test
    void shouldUseDefaultMaskedConfigValuePattern() {
        KafkaConnectExtensionProvider provider = new KafkaConnectExtensionProvider();
        provider.configure(Configuration.empty());

        Pattern pattern = provider.maskedConfigValuePattern();

        Assertions.assertNotNull(pattern);
        Assertions.assertTrue(pattern.matcher("••••").matches());
        Assertions.assertFalse(pattern.matcher("").matches());
        Assertions.assertFalse(pattern.matcher("secret").matches());
    }

    @Test
    void shouldDisableMaskedConfigValuePatternGivenEmptyValue() {
        KafkaConnectExtensionProvider provider = new KafkaConnectExtensionProvider();
        provider.configure(Configuration.from(Map.of("maskedConfigValuePattern", "")));

        Assertions.assertNull(provider.maskedConfigValuePattern());
    }

    @Test
    void shouldThrowConfigExceptionGivenInvalidMaskedConfigValuePattern() {
        KafkaConnectExtensionProvider provider = new KafkaConnectExtensionProvider();
        Configuration configuration = Configuration.from(Map.of("maskedConfigValuePattern", "["));

        Assertions.assertThrows(ConfigException.class, () -> provider.configure(configuration));
    }
}
