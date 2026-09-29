/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.common.utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.jikkou.core.exceptions.InvalidResourceFileException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public final class IOUtils {

    private static final String SYNTAX_GLOB = "glob:";
    private static final String SYNTAX_REGEX = "regex:";

    private IOUtils() {
    }

    /**
     * Finds all files matching the given glob pattern starting from the specified directory.
     * This method recursively traverses all subdirectories.
     *
     * @param startingDirectory the directory to start searching from.
     * @param pattern           the glob pattern to match files against (e.g., "**&#47;*.{yaml,yml}").
     * @return a list of paths matching the pattern.
     */
    public static List<Path> findMatching(final Path startingDirectory, final String pattern) {
        PathMatcher pathMatcher = getPathMatcher(pattern);
        // Create a secondary matcher for the filename only when pattern starts with **/
        // This handles files in the root directory since **/ requires at least one directory
        String normalizedPattern = pattern.startsWith(SYNTAX_GLOB) ? pattern.substring(SYNTAX_GLOB.length()) : pattern;
        normalizedPattern =
                normalizedPattern.startsWith(SYNTAX_REGEX) ? normalizedPattern.substring(SYNTAX_REGEX.length()) : normalizedPattern;
        PathMatcher fileNameMatcher =
                normalizedPattern.startsWith("**/") ? getPathMatcher(normalizedPattern.substring(3)) : null;
        try (Stream<Path> pathStream = Files.walk(startingDirectory)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        Path relativePath = startingDirectory.relativize(path);
                        if (pathMatcher.matches(relativePath)) {
                            return true;
                        }
                        // For files in root directory, also try matching just the filename
                        if (fileNameMatcher != null && relativePath.getNameCount() == 1) {
                            return fileNameMatcher.matches(relativePath.getFileName());
                        }
                        return false;
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to traverse directory: " + startingDirectory, e);
        }
    }

    public static PathMatcher getPathMatcher(final String pattern) {
        var syntaxAndPattern = isPrefixWithSyntax(pattern) ? pattern : SYNTAX_GLOB + pattern;
        return FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
    }

    private static boolean isPrefixWithSyntax(String pattern) {
        return pattern.startsWith(SYNTAX_REGEX) | pattern.startsWith(SYNTAX_GLOB);
    }

    public static String readTextFile(final String location) {
        try (InputStream stream = openStream(URI.create(location))) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isLocalDirectory(final URI location) {
        String scheme = location.getScheme();
        if (scheme == null)
            return Files.isDirectory(Paths.get(location.getPath()));
        if (scheme.equalsIgnoreCase("file"))
            return Files.isDirectory(Path.of(location));
        return false;
    }

    @NotNull
    public static InputStream newInputStream(final URI location) {
        try {
            return IOUtils.openStream(location);
        } catch (RuntimeException e) {
            Throwable t = e.getCause() != null ? e.getCause() : e;
            if (t instanceof NoSuchFileException) {
                throw new InvalidResourceFileException(
                    location,
                    "Failed to read '%s': No such file or directory.".formatted(location)
                );
            } else {
                throw new InvalidResourceFileException(
                    location,
                    "Failed to read '%s': %s".formatted(location, t.getMessage()));
            }
        }
    }

    @NotNull
    public static InputStream openStream(final URI location) {
        String scheme = location.getScheme();
        if (scheme == null)
            return openStream(Paths.get(location.getPath()));

        if (scheme.equalsIgnoreCase("file"))
            return openStream(Path.of(location));

        if (scheme.equalsIgnoreCase("http") ||
                scheme.equalsIgnoreCase("https")) {
            try {
                return openStream(location.toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        if (scheme.equalsIgnoreCase("classpath")) {
            String path = location.toString().replaceFirst("classpath://", "");
            URL resource = ClassLoader.getSystemResource(path);
            if (resource == null) {
                throw new RuntimeException(String.format("Cannot find resource from URI: '%s'", location));
            }
            return openStream(resource);
        }

        throw new RuntimeException(String.format(
                "Scheme '%s 'is not supported in given URI: '%s'",
                scheme,
                location
        )
        );
    }

    public static String getFileName(URI path) {
        return getFileName(path.normalize().getPath());
    }

    public static String getFileName(String path) {
        int idx = path.lastIndexOf("/");
        String filename = path;
        if (idx >= 0) {
            filename = path.substring(idx + 1);
        }
        return filename;
    }

    public static InputStream openStream(final String resource) {
        return new ByteArrayInputStream(resource.getBytes(StandardCharsets.UTF_8));
    }

    public static InputStream openStream(final Path url) {
        try {
            return Files.newInputStream(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream openStream(final URL url) {
        try {
            String protocol = url.getProtocol();
            if (!protocol.equalsIgnoreCase("http") && !protocol.equalsIgnoreCase("https")) {
                return new BufferedInputStream(url.openStream());
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            resolveBasicAuth(url.getHost()).ifPresent(credentials ->
                    connection.setRequestProperty("Authorization", "Basic " + credentials));
            return new BufferedInputStream(connection.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String HTTP_AUTHENTICATIONS_PATH = "jikkou.io.http.authentications";

    private static volatile Config httpAuthConfig;

    /**
     * Resolves the Base64-encoded Basic credentials configured for the given host, if any.
     *
     * <p>Credentials are read from the {@code jikkou.io.http.authentications} configuration list,
     * loaded lazily through a raw {@link ConfigFactory#load()} call. {@code JikkouConfig} is
     * deliberately not used here to avoid logging the resolved configuration (which may contain
     * secrets) when this code path executes.
     *
     * @param host the host to look up (exact, case-insensitive match).
     * @return the Base64-encoded {@code username:password} credentials, or empty.
     */
    private static Optional<String> resolveBasicAuth(final String host) {
        Config config = httpAuthConfig();
        if (!config.hasPath(HTTP_AUTHENTICATIONS_PATH)) {
            return Optional.empty();
        }
        for (Config entry : config.getConfigList(HTTP_AUTHENTICATIONS_PATH)) {
            if (!entry.hasPath("host") || !entry.getString("host").equalsIgnoreCase(host)) {
                continue;
            }
            if (!entry.hasPath("username") || !entry.hasPath("password")) {
                String missingKey = entry.hasPath("username") ? "password" : "username";
                throw new RuntimeException(String.format(
                        "Invalid configuration: entry for host '%s' in '%s' is missing key '%s'. "
                                + "If the value comes from an environment variable (e.g. ${?VAR}), "
                                + "make sure that variable is set.",
                        host, HTTP_AUTHENTICATIONS_PATH, missingKey));
            }
            String credentials = entry.getString("username") + ":" + entry.getString("password");
            return Optional.of(Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)));
        }
        return Optional.empty();
    }

    private static Config httpAuthConfig() {
        Config result = httpAuthConfig;
        if (result == null) {
            synchronized (IOUtils.class) {
                result = httpAuthConfig;
                if (result == null) {
                    result = ConfigFactory.load();
                    httpAuthConfig = result;
                }
            }
        }
        return result;
    }

    /**
     * Resets the cached configuration used for HTTP authentication. Visible for testing only.
     */
    static void resetHttpAuthConfig() {
        synchronized (IOUtils.class) {
            httpAuthConfig = null;
            ConfigFactory.invalidateCaches();
        }
    }
}
