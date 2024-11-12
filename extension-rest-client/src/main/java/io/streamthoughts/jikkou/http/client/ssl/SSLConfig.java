/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.ssl;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;

/**
 * SSL Configs.
 *
 * @param keyStoreLocation
 * @param keyStorePassword
 * @param keyStoreType
 * @param keyPassword
 * @param trustStoreLocation
 * @param trustStorePassword
 * @param trustStoreType
 */
public record SSLConfig(
    String keyStoreLocation,
    String keyStorePassword,
    String keyStoreType,
    String keyPassword,
    String trustStoreLocation,
    String trustStorePassword,
    String trustStoreType,
    boolean ignoreHostnameVerification
) {

    public static ConfigProperty<String> SSL_KEY_STORE_LOCATION = ConfigProperty
        .ofString("sslKeyStoreLocation")
        .description("The location of the key store file.");

    public static ConfigProperty<String> SSL_KEY_STORE_TYPE = ConfigProperty
        .ofString("sslKeyStoreType")
        .description("The file format of the key store file.")
        .defaultValue("PKCS12");

    public static ConfigProperty<String> SSL_KEY_STORE_PASSWORD = ConfigProperty
        .ofString("sslKeyStorePassword")
        .description("The password for the key store file.");


    public static ConfigProperty<String> sslKeyPassword = ConfigProperty
        .ofString("sslKeyPassword")
        .description("The password of the private key in the key store file.");


    public static ConfigProperty<String> SSL_TRUST_STORE_LOCATION = ConfigProperty
        .ofString("sslTrustStoreLocation")
        .description("The location of the trust store file.");


    public static ConfigProperty<String> SSL_TRUST_STORE_TYPE = ConfigProperty
        .ofString("sslTrustStoreType")
        .description("The file format of the trust store file.")
        .defaultValue("PKCS12");


    public static ConfigProperty<String> SSL_TRUST_STORE_PASSWORD = ConfigProperty
        .ofString("sslTrustStorePassword")
        .description("The password for the trust store file.");


    public static ConfigProperty<Boolean> SSL_IGNORE_HOSTNAME_VERIFICATION = ConfigProperty
        .ofBoolean("sslIgnoreHostnameVerification")
        .description("Specifies whether to ignore the hostname verification.")
        .defaultValue(false);


    public static SSLConfig from(final Configuration configuration) {
        return new SSLConfig(
            SSL_KEY_STORE_LOCATION.getOptional(configuration).orElse(null),
            SSL_KEY_STORE_PASSWORD.getOptional(configuration).orElse(null),
            SSL_KEY_STORE_TYPE.get(configuration),
            sslKeyPassword.getOptional(configuration).orElse(null),
            SSL_TRUST_STORE_LOCATION.getOptional(configuration).orElse(null),
            SSL_TRUST_STORE_PASSWORD.getOptional(configuration).orElse(null),
            SSL_TRUST_STORE_TYPE.get(configuration),
            SSL_IGNORE_HOSTNAME_VERIFICATION.get(configuration)
        );
    }
}