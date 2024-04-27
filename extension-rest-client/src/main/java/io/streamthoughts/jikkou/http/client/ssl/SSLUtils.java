/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Utility class for SSL.
 */
public final class SSLUtils {


    public static TrustManager[] createTrustManagers(final String trustStoreLocation,
                                                     final char[] trustStorePassword,
                                                     final String trustStoreType,
                                                     final String defaultAlgorithm)
        throws CertificateException,
        NoSuchAlgorithmException,
        KeyStoreException,
        IOException {
        KeyStore trustStore = null;
        if (trustStoreLocation != null)
            trustStore = createKeyStore(trustStoreLocation, trustStorePassword, trustStoreType);

        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(defaultAlgorithm);
        // Get default from cacerts if trustStore is null.
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();
    }

    public static KeyManager[] createKeyManagers(final String keyStoreLocation,
                                                 final char[] keyPassword,
                                                 final String keyStoreType,
                                                 final String defaultAlgorithm)
        throws UnrecoverableKeyException,
        NoSuchAlgorithmException,
        KeyStoreException,
        CertificateException,
        IOException {
        final KeyStore keyStore = createKeyStore(keyStoreLocation, keyPassword, keyStoreType);
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(defaultAlgorithm);
        keyManagerFactory.init(keyStore, keyPassword);
        return keyManagerFactory.getKeyManagers();
    }

    /**
     * Helper method to create a new {@link KeyStore}.
     *
     * @param keyStoreLocation the location of the Keystore file.
     * @param keyStorePassword the password of the Keystore.
     * @param keyStoreType     the type of the Keystore.
     * @return a new {@link KeyStore} instance.
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static KeyStore createKeyStore(final String keyStoreLocation,
                                          final char[] keyStorePassword,
                                          final String keyStoreType)
        throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        try (InputStream is = Files.newInputStream(Paths.get(keyStoreLocation))) {
            KeyStore ks = KeyStore.getInstance(keyStoreType);
            ks.load(is, keyStorePassword);
            return ks;
        }
    }
}
