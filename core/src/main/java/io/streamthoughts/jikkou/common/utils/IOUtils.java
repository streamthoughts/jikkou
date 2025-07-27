/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

import io.streamthoughts.jikkou.core.exceptions.InvalidResourceFileException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;

public final class IOUtils {

    private static final String SYNTAX_GLOB = "glob:";
    private static final String SYNTAX_REGEX = "regex:";

    private IOUtils() {
    }

    public static List<Path> findMatching(final Path startingDirectory,
                                          final String pattern) {
        PathMatcher pathMatcher = getPathMatcher(pattern);
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(startingDirectory, pathMatcher::matches)) {
            return StreamSupport.stream(dirStream.spliterator(), false).toList();
        } catch (IOException e) {
            throw new RuntimeException("", e);
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
            return new BufferedInputStream(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
