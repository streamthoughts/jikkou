/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.http.client.ssl.SSLConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SslConfigSupportTest {

    @Test
    void shouldCreateSslConfigWithoutNamespace() {
        Assertions.assertDoesNotThrow(() -> {
            SSLConfig.from(Configuration
                .builder()
                .with(SSLConfig.SSL_KEY_STORE_PASSWORD.key(), "password")
                .with(SSLConfig.SSL_KEY_STORE_LOCATION.key(), "/tmp/keystore.jks")
                .with(SSLConfig.SSL_KEY_STORE_TYPE.key(), "jks")
                .with(SSLConfig.SSL_TRUST_STORE_LOCATION.key(), "/tmp/truststore.jks")
                .with(SSLConfig.SSL_TRUST_STORE_PASSWORD.key(), "password")
                .with(SSLConfig.SSL_TRUST_STORE_TYPE.key(), "jks")
                .build()
            );
        });
    }
}