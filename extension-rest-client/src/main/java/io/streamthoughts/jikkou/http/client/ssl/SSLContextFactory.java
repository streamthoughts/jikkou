/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.ssl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default class for retrieving a new {@link SSLContext} instance.
 */
public class SSLContextFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SSLContextFactory.class);

    private static final String PROTOCOL_TLS = "TLS";

    /**
     * Gets the {@link SSLContext} instance using the specified configuration.
     *
     * @return  a new {@link SSLContext} instance.
     */
    public SSLContext getSSLContext(final KeyManager[] keyManagers,
                                    final TrustManager[] trustManagers) {
        try {
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL_TLS);
            sslContext.init(keyManagers, trustManagers, new SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException
                 | KeyManagementException e){
            LOG.error("Could not create SSL context for Client Certificate authentication.", e);
            throw new RuntimeException(e);
        }
    }
}
