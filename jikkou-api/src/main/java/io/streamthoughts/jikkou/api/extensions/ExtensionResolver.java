/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.api.extensions;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionResolver.class);

    private static boolean isArchiveFile(final Path path) {
        String lowerCased = path.toString().toLowerCase();
        return lowerCased.endsWith(".jar") || lowerCased.endsWith(".zip");
    }

    private static boolean isClassFile(final Path path) {
        return path.toString().toLowerCase().endsWith(".class");
    }

    private final Path extensionPath;

    /**
     * Creates a new {@link ExtensionResolver} instance.
     *
     * @param extensionPath the top-level component path.
     */
    public ExtensionResolver(final Path extensionPath) {
        Objects.requireNonNull(extensionPath, "extensionPath cannot be null");
        this.extensionPath = extensionPath;
    }

    public List<ExternalExtension> resolves() {
        List<ExternalExtension> components = new ArrayList<>();
        try (
                final DirectoryStream<Path> paths = Files.newDirectoryStream(extensionPath, entry -> {
                    return Files.isDirectory(entry) || isArchiveFile(entry);
                });
        ) {
            for (Path path : paths) {
                final List<URL> resources = resolveUrlsForComponentPath(path);
                components.add(new ExternalExtension(
                        path.toUri().toURL(),
                        resources.toArray(new URL[0]))
                );
            }
        } catch (Exception e) {
            LOG.warn(
                    "Failed to list extensions from path '{}'. {}",
                    extensionPath,
                    e.getMessage()
            );
        }
        return components;
    }

    /**
     * <p>
     * This method is inspired from the original class : org.apache.kafka.connect.runtime.isolation.PluginUtils.
     * from <a href="https://github.com/apache/kafka">Apache Kafka</a> project.
     * </p>
     *
     * @throws IOException if an error occurred while traversing the given path.
     */
    private static List<URL> resolveUrlsForComponentPath(final Path path) throws IOException {

        final List<Path> archives = new ArrayList<>();

        boolean containsClassFiles = false;
        if (isArchiveFile(path)) {
            archives.add(path);
        } else {

            LinkedList<Path> directories = new LinkedList<>();
            directories.add(path);

            while (!directories.isEmpty()) {
                final Path directory = directories.poll();
                try (
                        final DirectoryStream<Path> stream = Files.newDirectoryStream(directory, entry -> {
                            return Files.isDirectory(entry) || isArchiveFile(entry) || isClassFile(entry);
                        })
                ) {
                    for (Path entry : stream) {
                        if (isArchiveFile(entry)) {
                            LOG.debug("Detected Extension jar: {}", entry);
                            archives.add(entry);
                        } else if (isClassFile(entry)) {
                            LOG.debug("Detected Extension class file: {}", entry);
                            containsClassFiles = true;
                        } else {
                            directories.add(entry);
                        }
                    }
                } catch (final InvalidPathException e) {
                    LOG.error("Invalid extension path '{}', path ignored.", directory, e);
                } catch (IOException e) {
                    LOG.error("Error while listing extension path '{}' path ignored.", directory, e);
                }
            }
        }

        if (containsClassFiles) {
            if (archives.isEmpty()) {
                return Collections.singletonList(path.toUri().toURL());
            }
            LOG.error(
                    "Extension path '{}' contains both java class files and JARs, " +
                     "class files will be ignored and only archives will be scanned.", path);
        }

        List<URL> urls = new ArrayList<>(archives.size());
        for (Path archive : archives) {
            urls.add(archive.toUri().toURL());
        }
        return urls;
    }
}
