/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.http.client.ssl.SSLConfig;

public interface SslConfigSupport {

    static ConfigProperty<String> sslKeyStoreLocation(final String configNamespace) {
        return ConfigProperty
            .ofString(configNamespace + ".sslKeyStoreLocation")
            .description("The location of the key store file.");
    }

    static ConfigProperty<String> sslKeyStoreType(final String configNamespace) {
        return ConfigProperty
            .ofString(configNamespace + ".sslKeyStoreType")
            .description("The file format of the key store file.")
            .orElse("PKCS12");
    }

    static ConfigProperty<String> sslKeyStorePassword(final String configNamespace) {
        return ConfigProperty
            .ofString(configNamespace + ".sslKeyStorePassword")
            .description("The password for the key store file.");
    }

    static ConfigProperty<String> sslKeyPassword(final String configNamespace) {
        return ConfigProperty
            .ofString(configNamespace + ".sslKeyPassword")
            .description("The password of the private key in the key store file.");
    }

    static ConfigProperty<String> sslTrustStoreLocation(final String configNamespace) {
        return ConfigProperty
            .ofString(configNamespace + ".sslTrustStoreLocation")
            .description("The location of the trust store file.");
    }

    static ConfigProperty<String> sslTrustStoreType(final String configNamespace) {
        return ConfigProperty
            .ofString(configNamespace + ".sslTrustStoreType")
            .description("The file format of the trust store file.")
            .orElse("PKCS12");
    }

    static ConfigProperty<String> sslTrustStorePassword(final String configNamespace) {
        return ConfigProperty
            .ofString(configNamespace + ".sslTrustStorePassword")
            .description("The password for the trust store file.");
    }

    static ConfigProperty<Boolean> sslIgnoreHostnameVerification(final String configNamespace) {
        return ConfigProperty
            .ofBoolean(configNamespace + ".sslIgnoreHostnameVerification")
            .description("Specifies whether to ignore the hostname verification.")
            .orElse(false);
    }

    static SSLConfig getSslConfig(final String configNamespace,
                                  final Configuration configuration) {
        return new SSLConfig(
            SslConfigSupport.sslKeyStoreLocation(configNamespace).getOptional(configuration).orElse(null),
            SslConfigSupport.sslKeyStorePassword(configNamespace).getOptional(configuration).orElse(null),
            SslConfigSupport.sslKeyStoreType(configNamespace).get(configuration),
            SslConfigSupport.sslKeyPassword(configNamespace).getOptional(configuration).orElse(null),
            SslConfigSupport.sslTrustStoreLocation(configNamespace).getOptional(configuration).orElse(null),
            SslConfigSupport.sslTrustStorePassword(configNamespace).getOptional(configuration).orElse(null),
            SslConfigSupport.sslTrustStoreType(configNamespace).get(configuration),
            SslConfigSupport.sslIgnoreHostnameVerification(configNamespace).get(configuration)
        );
    }
}
