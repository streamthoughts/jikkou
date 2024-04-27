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
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface SslConfigSupport {

    String SSL_KEY_STORE_LOCATION = "sslKeyStoreLocation";
    String SSL_KEY_STORE_TYPE = "sslKeyStoreType";
    String SSL_KEY_STORE_PASSWORD = "sslKeyStorePassword";
    String SSL_KEY_PASSWORD = "sslKeyPassword";
    String SSL_TRUST_STORE_LOCATION = "sslTrustStoreLocation";
    String SSL_TRUST_STORE_PASSWORD = "sslTrustStorePassword";
    String SSL_TRUST_STORE_TYPE = "sslTrustStoreType";
    String SSL_IGNORE_HOSTNAME_VERIFICATION = "sslIgnoreHostnameVerification";

    static ConfigProperty<String> sslKeyStoreLocation(final String configNamespace) {
        return ConfigProperty
            .ofString(prefixWithNamespace(configNamespace, SSL_KEY_STORE_LOCATION))
            .description("The location of the key store file.");
    }

    private static @NotNull String prefixWithNamespace(final String configNamespace,
                                                       final String sslKeyStoreLocation) {
        return Optional.ofNullable(configNamespace).map(s -> s + ".").orElse("") + sslKeyStoreLocation;
    }

    static ConfigProperty<String> sslKeyStoreType(final String configNamespace) {
        return ConfigProperty
            .ofString(prefixWithNamespace(configNamespace, SSL_KEY_STORE_TYPE))
            .description("The file format of the key store file.")
            .orElse("PKCS12");
    }

    static ConfigProperty<String> sslKeyStorePassword(final String configNamespace) {
        return ConfigProperty
            .ofString(prefixWithNamespace(configNamespace, SSL_KEY_STORE_PASSWORD))
            .description("The password for the key store file.");
    }

    static ConfigProperty<String> sslKeyPassword(final String configNamespace) {
        return ConfigProperty
            .ofString(prefixWithNamespace(configNamespace, SSL_KEY_PASSWORD))
            .description("The password of the private key in the key store file.");
    }

    static ConfigProperty<String> sslTrustStoreLocation(final String configNamespace) {
        return ConfigProperty
            .ofString(prefixWithNamespace(configNamespace, SSL_TRUST_STORE_LOCATION))
            .description("The location of the trust store file.");
    }

    static ConfigProperty<String> sslTrustStoreType(final String configNamespace) {
        return ConfigProperty
            .ofString(prefixWithNamespace(configNamespace, SSL_TRUST_STORE_TYPE))
            .description("The file format of the trust store file.")
            .orElse("PKCS12");
    }

    static ConfigProperty<String> sslTrustStorePassword(final String configNamespace) {
        return ConfigProperty
            .ofString(prefixWithNamespace(configNamespace, SSL_TRUST_STORE_PASSWORD))
            .description("The password for the trust store file.");
    }

    static ConfigProperty<Boolean> sslIgnoreHostnameVerification(final String configNamespace) {
        return ConfigProperty
            .ofBoolean(prefixWithNamespace(configNamespace, SSL_IGNORE_HOSTNAME_VERIFICATION))
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
