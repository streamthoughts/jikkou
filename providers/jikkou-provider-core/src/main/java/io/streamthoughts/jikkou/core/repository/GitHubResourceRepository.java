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

    /**
     * The Extension config
     */
    public interface Config {
        ConfigProperty<String> REPOSITORY_CONFIG = ConfigProperty
            .ofString("repository")
            .description("Specify the GitHub repository in the format 'owner/repo'");

        ConfigProperty<String> BRANCH_CONFIG = ConfigProperty
            .ofString("branch")
            .description("Specify the branch or ref to load resources from")
            .defaultValue("main");

        ConfigProperty<List<String>> PATHS_CONFIG = ConfigProperty
            .ofList("paths")
            .description("Specify the paths/directories in the repository containing the resource definitions")
            .defaultValue(List.of("."));

        ConfigProperty<String> FILE_PATTERN_CONFIG = ConfigProperty
            .ofString("file-pattern")
            .description("""
                Specify the pattern used to match YAML file paths.
                Pattern should be passed in the form of 'syntax:pattern'. The "glob" and "regex" syntaxes are supported (e.g.: **/*.{yaml,yml}).
                If no syntax is specified the 'glob' syntax is used.
                """)
            .defaultValue("**/*.{yaml,yml}");

        ConfigProperty<String> TOKEN_CONFIG = ConfigProperty
            .ofString("token")
            .description("GitHub personal access token for authentication (optional for public repositories)")
            .defaultValue("");

        String VALUES_FILES_DESCRIPTION = "Specify the paths of the values-files containing the variables to pass into the template engine";
        ConfigProperty<List<String>> VALUE_FILES_CONFIG = ConfigProperty
            .ofList("values-files")
            .description(VALUES_FILES_DESCRIPTION)
            .defaultValue(List.of());

        String VALUES_FILE_PATTERN_DESCRIPTION = """
            Specify the pattern used to match values YAML file paths.
            Pattern should be passed in the form of 'syntax:pattern'. The "glob" and "regex" syntaxes are supported.
            If no syntax is specified the 'glob' syntax is used.
            """;
        ConfigProperty<String> VALUE_FILE_PATTERN_CONFIG = ConfigProperty
            .ofString("values-file-pattern")
            .description(VALUES_FILE_PATTERN_DESCRIPTION)
            .defaultValue("**/*.{yaml,yml}");

        String LABELS_DESCRIPTION = "Set labels on the command line (can specify multiple values)";
        ConfigProperty<Map<String, Object>> LABEL_CONFIG = ConfigProperty
            .ofMap("labels")
            .description(LABELS_DESCRIPTION)
            .defaultValue(Map::of);
    }

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
        return all(new GitHubResourceOptions(extensionContext().configuration())).getItems();
    }

    public HasItems all(final GitHubResourceOptions options) {
        try {
            // Load values first
            NamedValueSet values = loadValuesFromGitHub(options);
            values = values.with(options.getValues());

            // Create resource reader options
            ResourceReaderOptions readerOptions = ResourceReaderOptions.DEFAULTS
                .withLabels(NamedValueSet.setOf(options.getLabels()))
                .withValues(values)
                .withPattern(options.getResourceFilePattern());

            // Load resources from GitHub and combine them
            List<HasMetadata> allResources = new ArrayList<>();
            ResourceLoader loader = new ResourceLoader(resourceReaderFactory, readerOptions);

            for (String path : options.getResourceFileLocations()) {
                List<GitHubFile> files = listFilesInPath(options.getRepository(), options.getBranch(), path, options.getToken());

                for (GitHubFile file : files) {
                    if (file.path().endsWith(".yaml") || file.path().endsWith(".yml")) {
                        String content = downloadFileContent(file, options.getToken());
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

    private NamedValueSet loadValuesFromGitHub(GitHubResourceOptions options) throws Exception {
        if (options.getValuesFileLocations().isEmpty()) {
            return NamedValueSet.emptySet();
        }

        NamedValueSet result = NamedValueSet.emptySet();

        for (String path : options.getValuesFileLocations()) {
            List<GitHubFile> files = listFilesInPath(options.getRepository(), options.getBranch(), path, options.getToken());

            for (GitHubFile file : files) {
                if (file.path().endsWith(".yaml") || file.path().endsWith(".yml")) {
                    String content = downloadFileContent(file, options.getToken());
                    try (InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                        NamedValueSet values = ValuesLoader.loadFromInputStreams(
                            List.of(Pair.of(stream, file.uri())),
                            ValuesReaderOptions.of(options.getValuesFilePattern())
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
     * Configuration for GitHub resource loading
     */
    public record GitHubResourceOptions(Configuration configuration) {

        public String getRepository() {
            return Config.REPOSITORY_CONFIG.get(configuration);
        }

        public String getBranch() {
            return Config.BRANCH_CONFIG.get(configuration);
        }

        public List<String> getResourceFileLocations() {
            return Config.PATHS_CONFIG.get(configuration);
        }

        public String getResourceFilePattern() {
            return Config.FILE_PATTERN_CONFIG.get(configuration);
        }

        public List<String> getValuesFileLocations() {
            return Config.VALUE_FILES_CONFIG.get(configuration);
        }

        public String getValuesFilePattern() {
            return Config.VALUE_FILE_PATTERN_CONFIG.get(configuration);
        }

        public String getToken() {
            return Config.TOKEN_CONFIG.get(configuration);
        }

        public NamedValueSet getValues() {
            return NamedValueSet.emptySet();
        }

        public NamedValueSet getLabels() {
            return Config.LABEL_CONFIG.getOptional(configuration)
                .map(NamedValueSet::setOf)
                .orElse(NamedValueSet.emptySet());
        }
    }
}