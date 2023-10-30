/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.http.client.security;

import io.streamthoughts.jikkou.http.client.exception.JikkouApiClientException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Default class for retrieving a new {@link SSLContext} instance. */
public class SSLContextFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SSLContextFactory.class);

    private static final String PROTOCOL_TLS = "TLS";

    /**
     * Gets the {@link SSLContext} instance using the specified configuration.
     *
     * @return a new {@link SSLContext} instance.
     */
    public SSLContext getSSLContext(
            final KeyManager[] keyManagers, final TrustManager[] trustManagers) {
        try {
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL_TLS);
            sslContext.init(keyManagers, trustManagers, new SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LOG.error("Could not create SSL context for Client Certificate authentication.", e);
            throw new JikkouApiClientException(e);
        }
    }
}