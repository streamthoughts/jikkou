/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client;

import io.streamthoughts.jikkou.core.config.Configuration;
import org.junit.jupiter.api.Test;

class SslConfigSupportTest {

    @Test
    void shouldCreateSslConfigWithoutNamespace() {
        SslConfigSupport.getSslConfig(null, Configuration
            .builder()
                .with(SslConfigSupport.SSL_KEY_STORE_PASSWORD, "password")
                .with(SslConfigSupport.SSL_KEY_STORE_LOCATION, "/tmp/keystore.jks")
                .with(SslConfigSupport.SSL_KEY_STORE_TYPE, "jks")
                .with(SslConfigSupport.SSL_TRUST_STORE_LOCATION, "/tmp/truststore.jks")
                .with(SslConfigSupport.SSL_TRUST_STORE_PASSWORD, "password")
                .with(SslConfigSupport.SSL_TRUST_STORE_TYPE, "jks")
            .build()
        );
    }
}