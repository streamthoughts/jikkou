/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.io;

import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.JikkouRuntimeException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Applies per-host HTTP Basic authentication to connections used for fetching remote resources.
 *
 * <p>Credentials are configured through the {@code jikkou.io.http.authentications} property:
 * <pre>{@code
 * jikkou.io.http.authentications: [
 *   { host: "repo.example.com", username: ${?REPO_USERNAME}, password: ${?REPO_PASSWORD} }
 * ]
 * }</pre>
 *
 * <p>The authenticator is configured once from the application's {@link Configuration}
 * (see {@link #configure(HttpAuthenticator)}) and is then available globally
 * through {@link #get()}.
 */
public final class HttpAuthenticator {

    /** The configuration key for the authentications list, relative to the {@code jikkou} root. */
    public static final String CONFIG_KEY = "io.http.authentications";

    private static volatile HttpAuthenticator current = new HttpAuthenticator(List.of());

    private final List<HostCredentials> entries;

    /**
     * The Basic credentials associated with a host.
     *
     * @param host     the host to match (exact, case-insensitive).
     * @param username the username.
     * @param password the password.
     */
    public record HostCredentials(String host, String username, String password) {
    }

    /**
     * Creates a new {@link HttpAuthenticator} instance.
     *
     * @param entries the host credentials entries.
     */
    public HttpAuthenticator(@NotNull final List<HostCredentials> entries) {
        this.entries = List.copyOf(entries);
    }

    /**
     * Creates a new {@link HttpAuthenticator} from the given configuration.
     *
     * <p>Entries are parsed eagerly, but credentials are only validated when a connection
     * actually targets a matching host (see {@link #authenticate(HttpURLConnection)}). This
     * allows credentials to come from optional environment variable substitutions
     * (e.g. {@code ${?VAR}}): a command that never fetches from a configured host is not
     * affected by missing variables.
     *
     * <p>Entries without a {@code host} key can never match and are ignored.
     *
     * @param config the configuration.
     * @return a new {@link HttpAuthenticator} instance.
     */
    public static HttpAuthenticator fromConfiguration(@NotNull final Configuration config) {
        if (!config.hasKey(CONFIG_KEY)) {
            return new HttpAuthenticator(List.of());
        }
        List<HostCredentials> entries = config.getConfigList(CONFIG_KEY).stream()
                .filter(entry -> entry.hasKey("host"))
                .map(entry -> new HostCredentials(
                        entry.getString("host"),
                        entry.hasKey("username") ? entry.getString("username") : null,
                        entry.hasKey("password") ? entry.getString("password") : null))
                .toList();
        return new HttpAuthenticator(entries);
    }

    /**
     * Sets the {@link HttpAuthenticator} to be used globally.
     *
     * @param authenticator the authenticator.
     */
    public static void configure(@NotNull final HttpAuthenticator authenticator) {
        current = authenticator;
    }

    /**
     * Gets the globally configured {@link HttpAuthenticator}.
     *
     * @return the current authenticator; never {@code null}.
     */
    public static HttpAuthenticator get() {
        return current;
    }

    /**
     * Sets an {@code Authorization: Basic} header on the given connection if credentials
     * are configured for the connection's host. Otherwise, the connection is left untouched.
     *
     * @param connection the connection to authenticate.
     * @throws JikkouRuntimeException if the connection's host matches an entry with missing
     *         credentials (e.g. because a referenced environment variable is not set).
     */
    public void authenticate(@NotNull final HttpURLConnection connection) {
        entries.stream()
                .filter(entry -> entry.host().equalsIgnoreCase(connection.getURL().getHost()))
                .findFirst()
                .ifPresent(entry -> {
                    if (entry.username() == null || entry.password() == null) {
                        String missingKey = entry.username() == null ? "username" : "password";
                        throw new JikkouRuntimeException(String.format(
                                "Invalid configuration: the entry for host '%s' in '%s' is missing key '%s'. "
                                        + "If the value comes from an environment variable "
                                        + "(e.g. ${?VAR}), make sure that variable is set.",
                                entry.host(), CONFIG_KEY, missingKey));
                    }
                    String credentials = entry.username() + ":" + entry.password();
                    String encoded = Base64.getEncoder()
                            .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
                    connection.setRequestProperty("Authorization", "Basic " + encoded);
                });
    }
}
