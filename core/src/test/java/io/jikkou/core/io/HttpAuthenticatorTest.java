/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.io;

import io.jikkou.core.exceptions.JikkouRuntimeException;
import io.jikkou.runtime.JikkouConfig;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HttpAuthenticatorTest {

    @AfterEach
    void reset() {
        HttpAuthenticator.configure(new HttpAuthenticator(List.of()));
    }

    @Test
    void shouldReturnEmptyAuthenticator_whenNoConfigEntry() {
        // Given
        JikkouConfig config = JikkouConfig.create(Map.of("some", "value"), false);

        // When
        HttpAuthenticator authenticator = HttpAuthenticator.fromConfiguration(config);

        // Then
        Assertions.assertNotNull(authenticator);
    }

    @Test
    void shouldParseEntries_whenConfigContainsAuthentications() {
        // Given
        JikkouConfig config = JikkouConfig.create(
                Map.of(HttpAuthenticator.CONFIG_KEY, List.of(
                        Map.of("host", "repo.example.com", "username", "user", "password", "pass"))),
                false);

        // When
        HttpAuthenticator authenticator = HttpAuthenticator.fromConfiguration(config);

        // Then
        Assertions.assertNotNull(authenticator);
    }

    @Test
    void shouldThrowMeaningfulError_whenHostMatchesButCredentialsMissing() throws Exception {
        // Given
        HttpAuthenticator authenticator = HttpAuthenticator.fromConfiguration(JikkouConfig.create(
                Map.of(HttpAuthenticator.CONFIG_KEY, List.of(
                        Map.of("host", "repo.example.com", "username", "user"))),
                false));
        HttpURLConnection connection =
                (HttpURLConnection) URI.create("http://repo.example.com/schema.avsc").toURL().openConnection();

        // When
        JikkouRuntimeException exception = Assertions.assertThrows(
                JikkouRuntimeException.class, () -> authenticator.authenticate(connection));

        // Then
        Assertions.assertTrue(exception.getMessage().contains("password"));
        Assertions.assertTrue(exception.getMessage().contains(HttpAuthenticator.CONFIG_KEY));
    }
}
