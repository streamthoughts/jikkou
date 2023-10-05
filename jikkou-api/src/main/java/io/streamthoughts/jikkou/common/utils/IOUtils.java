/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.common.utils;

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
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.StreamSupport;

public final class IOUtils {

    private static final String SYNTAX_GLOB = "glob:";
    private static final String SYNTAX_REGEX = "regex:";

    private IOUtils() {
    }

    public static List<Path> findMatching(final Path startingDirectory,
                                          final String pattern) {
        var syntaxAndPattern = isPrefixWithSyntax(pattern) ? pattern : SYNTAX_GLOB + pattern;
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(startingDirectory, pathMatcher::matches)) {
            return StreamSupport.stream(dirStream.spliterator(), false).toList();
        } catch (IOException e) {
            throw new RuntimeException("", e);
        }
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
        if (scheme == null || scheme.equalsIgnoreCase("file"))
            return Files.isDirectory(Paths.get(location.getPath()));
        return false;
    }

    public static InputStream openStream(final URI location) {
        String scheme = location.getScheme();
        if (scheme == null)
            return openStream(Paths.get(location.getPath()));

        if (scheme.equalsIgnoreCase("file"))
            return openStream(Paths.get(location));

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
