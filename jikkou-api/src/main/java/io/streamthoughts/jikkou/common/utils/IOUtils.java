/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

    private IOUtils() {}

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
        return readTextFile(URI.create(location));
    }

    public static String readTextFile(final URI location) {
        String scheme = location.getScheme();
        if (scheme == null)
            return readTextFile(Paths.get(location.getPath()));

        if (scheme.equalsIgnoreCase("file"))
            return readTextFile(Paths.get(location));

        if (scheme.equalsIgnoreCase("http") ||
            scheme.equalsIgnoreCase("https")) {
            try {
                return readTextFile(location.toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException(String.format(
                "Scheme '%s 'is not supported in given URI: '%s'",
                scheme,
                location
                )
        );
    }

    public static String readTextFile(final Path url) {
        try(InputStream stream = Files.newInputStream(url)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readTextFile(final URL url) {
        try(BufferedInputStream stream = new BufferedInputStream(url.openStream())) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
