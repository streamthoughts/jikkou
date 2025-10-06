/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.repository;

import static io.streamthoughts.jikkou.core.io.Jackson.YAML_OBJECT_MAPPER;

import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.api.template.JinjaResourceTemplateRenderer;
import io.streamthoughts.jikkou.common.utils.IOUtils;
import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.io.ResourceLoader;
import io.streamthoughts.jikkou.core.io.ValuesLoader;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderFactory;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderOptions;
import io.streamthoughts.jikkou.core.io.reader.ValuesReaderOptions;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.template.ResourceTemplateRenderer;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ResourceRepository} implementation for loading resources from GitHub repository.
 */
public class GitHubResourceRepository extends ContextualExtension implements ResourceRepository {

    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String GITHUB_RAW_BASE = "https://raw.githubusercontent.com";
    private static final Duration HTTP_CLIENT_CONNECT_DURATION = Duration.ofSeconds(30);

    private ResourceReaderFactory resourceReaderFactory;
    private HttpClient httpClient;

    /**
     * Creates a new {@link GitHubResourceRepository} instance.
     */
    public GitHubResourceRepository() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        ResourceTemplateRenderer renderer = new JinjaResourceTemplateRenderer()
            .withPreserveRawTags(false)
            .withFailOnUnknownTokens(false);
        this.resourceReaderFactory = new ResourceReaderFactory(YAML_OBJECT_MAPPER, renderer);
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(HTTP_CLIENT_CONNECT_DURATION)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends HasMetadata> all() {
        return all(Config.of(extensionContext().configuration())).getItems();
    }

    public HasItems all(final Config configs) {
        try {
            // Load values first
            NamedValueSet values = loadValuesFromGitHub(configs);
            values = values.with(configs.values());

            // Create resource reader options
            ResourceReaderOptions readerOptions = ResourceReaderOptions.DEFAULTS
                .withLabels(NamedValueSet.setOf(configs.labels()))
                .withValues(values)
                .withPattern(configs.resourceFilePattern());

            // Load resources from GitHub and combine them
            List<HasMetadata> allResources = new ArrayList<>();
            ResourceLoader loader = new ResourceLoader(resourceReaderFactory, readerOptions);

            PathMatcher pathMatcher = IOUtils.getPathMatcher(configs.resourceFilePattern());
            for (String path : configs.resourceFileLocations()) {
                List<GitHubFile> files = listFilesInPath(configs.repository(), configs.branch(), path, configs.token());

                for (GitHubFile file : files) {
                    if (pathMatcher.matches(Path.of(file.path()))) {
                        String content = downloadFileContent(file, configs.token());
                        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                            HasItems items = loader.load(inputStream, file.uri());
                            allResources.addAll(items.getItems());
                        }
                    }
                }
            }

            return () -> allResources;

        } catch (Exception e) {
            throw new JikkouRuntimeException("Failed to load resources from GitHub repository", e);
        }
    }

    private NamedValueSet loadValuesFromGitHub(Config configs) throws Exception {
        if (configs.valuesFileLocations().isEmpty()) {
            return NamedValueSet.emptySet();
        }

        NamedValueSet result = NamedValueSet.emptySet();

        for (String path : configs.valuesFileLocations()) {
            List<GitHubFile> files = listFilesInPath(configs.repository(), configs.branch(), path, configs.token());

            for (GitHubFile file : files) {
                if (file.path().endsWith(".yaml") || file.path().endsWith(".yml")) {
                    String content = downloadFileContent(file, configs.token());
                    try (InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                        NamedValueSet values = ValuesLoader.loadFromInputStreams(
                            List.of(Pair.of(stream, file.uri())),
                            ValuesReaderOptions.of(configs.valuesFilePattern())
                        );
                        result = result.with(values);
                    }
                }
            }
        }

        return result;
    }

    private List<GitHubFile> listFilesInPath(String repository, String branch, String path, String token) throws Exception {
        String url = String.format("%s/repos/%s/contents/%s?ref=%s", GITHUB_API_BASE, repository, path, branch);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(new URI(url))
            .GET()
            .timeout(HTTP_CLIENT_CONNECT_DURATION);

        if (!token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        requestBuilder.header("Accept", "application/vnd.github.v3+json");

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to list files in GitHub repository: " + response.body());
        }

        JsonNode jsonResponse = Jackson.json().readTree(response.body());
        List<GitHubFile> result = new ArrayList<>();

        if (jsonResponse.isArray()) {
            for (JsonNode item : jsonResponse) {
                String type = item.get("type").asText();
                String itemPath = item.get("path").asText();

                if ("file".equals(type)) {
                    result.add(new GitHubFile(repository, branch, itemPath, type));
                } else if ("dir".equals(type)) {
                    // Recursively list files in subdirectories
                    result.addAll(listFilesInPath(repository, branch, itemPath, token));
                }
            }
        }

        return result;
    }

    private String downloadFileContent(GitHubFile file, String token) throws Exception {

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(file.uri())
            .GET()
            .timeout(HTTP_CLIENT_CONNECT_DURATION);

        if (!token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to download file from GitHub: " + file.path());
        }

        return response.body();
    }

    private record GitHubFile(String repository, String branch, String path, String type) {

        public URI uri() {
            try {
                return new URI("%s/%s/%s/%s".formatted(GITHUB_RAW_BASE, repository, branch, path));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * The Extension config
     */
    public record Config(
        String repository,
        String branch,
        List<String> resourceFileLocations,
        String resourceFilePattern,
        List<String> valuesFileLocations,
        String valuesFilePattern,
        String token,
        NamedValueSet values,
        NamedValueSet labels
    ) {

        public static final ConfigProperty<String> REPOSITORY_CONFIG = ConfigProperty
            .ofString("repository")
            .description("Specify the GitHub repository in the format 'owner/repo'");

        public static final ConfigProperty<String> BRANCH_CONFIG = ConfigProperty
            .ofString("branch")
            .description("Specify the branch or ref to load resources from")
            .defaultValue("main");

        public static final ConfigProperty<List<String>> PATHS_CONFIG = ConfigProperty
            .ofList("paths")
            .description("Specify the paths/directories in the repository containing the resource definitions")
            .defaultValue(List.of("."));

        public static final ConfigProperty<String> FILE_PATTERN_CONFIG = ConfigProperty
            .ofString("file-pattern")
            .description("""
                Specify the pattern used to match YAML file paths.
                Pattern should be passed in the form of 'syntax:pattern'. The "glob" and "regex" syntaxes are supported (e.g.: **/*.{yaml,yml}).
                If no syntax is specified the 'glob' syntax is used.
                """)
            .defaultValue("**/*.{yaml,yml}");

        public static final ConfigProperty<String> TOKEN_CONFIG = ConfigProperty
            .ofString("token")
            .description("GitHub personal access token for authentication (optional for public repositories)")
            .defaultValue("");

        public static final ConfigProperty<List<String>> VALUE_FILES_CONFIG = ConfigProperty
            .ofList("values-files")
            .description("Specify the paths of the values-files containing the variables to pass into the template engine")
            .defaultValue(List.of());

        public static final ConfigProperty<String> VALUE_FILE_PATTERN_CONFIG = ConfigProperty
            .ofString("values-file-pattern")
            .description("""
                Specify the pattern used to match values YAML file paths.
                Pattern should be passed in the form of 'syntax:pattern'. The "glob" and "regex" syntaxes are supported.
                If no syntax is specified the 'glob' syntax is used.
                """)
            .defaultValue("**/*.{yaml,yml}");

        public static final ConfigProperty<Map<String, Object>> LABEL_CONFIG = ConfigProperty
            .ofMap("labels")
            .description("The labels to be added to all resources loaded from the repository")
            .defaultValue(Map::of);

        public static Config of(final Configuration c) {
            return new Config(
                REPOSITORY_CONFIG.get(c),
                BRANCH_CONFIG.get(c),
                PATHS_CONFIG.get(c),
                FILE_PATTERN_CONFIG.get(c),
                VALUE_FILES_CONFIG.get(c),
                VALUE_FILE_PATTERN_CONFIG.get(c),
                TOKEN_CONFIG.get(c),
                NamedValueSet.emptySet(),
                LABEL_CONFIG.getOptional(c)
                    .map(NamedValueSet::setOf)
                    .orElse(NamedValueSet.emptySet())
            );
        }
    }
}