/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.repository;

import static io.streamthoughts.jikkou.core.io.Jackson.YAML_OBJECT_MAPPER;

import io.streamthoughts.jikkou.api.template.JinjaResourceTemplateRenderer;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.io.ResourceLoader;
import io.streamthoughts.jikkou.core.io.ValuesLoader;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderFactory;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderOptions;
import io.streamthoughts.jikkou.core.io.reader.ValuesReaderOptions;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.template.ResourceTemplateRenderer;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ResourceRepository} implementation for loading resources from local file-system.
 */
public class LocalResourceRepository extends ContextualExtension implements ResourceRepository {

    /**
     * The Extension config
     */
    public interface Config {
        String FILE_DESCRIPTION = "Specify the locations containing the definitions for resources in a YAML file, a directory.";

        ConfigProperty<List<String>> FILES_CONFIG = ConfigProperty
            .ofList("files")
            .description(FILE_DESCRIPTION)
            .defaultValue(List.of());

        String FILE_NAME_DESCRIPTION = """
            Specify the pattern used to match YAML file paths when one or multiple directories are given through the files argument.
            Pattern should be passed in the form of 'syntax:pattern'. The "glob" and "regex" syntaxes are supported (e.g.: **/*.{yaml,yml}).
            If no syntax is specified the 'glob' syntax is used.
            """;
        ConfigProperty<String> FILE_NAME_CONFIG = ConfigProperty
            .ofString("file-name")
            .description(FILE_NAME_DESCRIPTION)
            .defaultValue("**/*.{yaml,yml}");

        String VALUES_FILES_DESCRIPTION = "Specify the locations of the values-files containing the variables to pass into the template engine built-in object 'Values'.";

        ConfigProperty<List<String>> VALUE_FILES_CONFIG = ConfigProperty
            .ofList("values-files")
            .description(VALUES_FILES_DESCRIPTION)
            .defaultValue(List.of());

        String VALUES_FILE_NAME_DESCRIPTION = """
            Specify the pattern used to match YAML file paths when one or multiple directories are given through the 'values-files' argument.
            Pattern should be passed in the form of 'syntax:pattern'. The "glob" and "regex" syntaxes are supported (e.g.: **/*.{yaml,yml}).
            If no syntax is specified the 'glob' syntax is used.
            """;
        ConfigProperty<String> VALUE_FILE_NAME_CONFIG = ConfigProperty
            .ofString("values-file-name")
            .description(VALUES_FILE_NAME_DESCRIPTION)
            .defaultValue("**/*.{yaml,yml}");

        String LABELS_DESCRIPTION = "Set labels on the command line (can specify multiple values)";
        ConfigProperty<Map<String, Object>> LABEL_CONFIG = ConfigProperty
            .ofMap("labels")
            .description(LABELS_DESCRIPTION)
            .defaultValue(Map::of);
    }

    private ResourceTemplateRenderer renderer;

    private ResourceReaderFactory resourceReaderFactory;

    /**
     * Creates a new {@link LocalResourceRepository} instance.
     */
    public LocalResourceRepository() {
    }

    /**
     * Creates a new {@link LocalResourceRepository} instance.
     *
     * @param renderer The {@link ResourceTemplateRenderer}.
     */
    public LocalResourceRepository(final ResourceTemplateRenderer renderer) {
        this.renderer = renderer;
        this.resourceReaderFactory = new ResourceReaderFactory(YAML_OBJECT_MAPPER, renderer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        if (renderer == null) {
            renderer = new JinjaResourceTemplateRenderer()
                .withPreserveRawTags(false)
                .withFailOnUnknownTokens(false);
        }
        resourceReaderFactory = new ResourceReaderFactory(YAML_OBJECT_MAPPER, renderer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends HasMetadata> all() {
        return all(new InternalLocalResourceOptions(extensionContext().configuration())).getItems();
    }

    public HasItems all(final LocalResourceOptions options) {
        return new ResourceLoader(resourceReaderFactory, createResourceReaderOptions(options))
            .load(options.getResourceFileLocations());
    }

    @NotNull
    private ResourceReaderOptions createResourceReaderOptions(final LocalResourceOptions inputs) {
        NamedValueSet values = NamedValueSet.emptySet();
        if (!inputs.getValuesFileLocations().isEmpty()) {
            values = new ValuesLoader(YAML_OBJECT_MAPPER)
                .load(inputs.getValuesFileLocations(), ValuesReaderOptions.of(inputs.getValuesFilePattern()));
        }
        values = values.with(inputs.getValues());

        return ResourceReaderOptions.DEFAULTS
            .withLabels(NamedValueSet.setOf(inputs.getLabels()))
            .withValues(values)
            .withPattern(inputs.getResourceFilePattern());
    }

    private record InternalLocalResourceOptions(Configuration configuration) implements LocalResourceOptions {

        @Override
        public List<String> getResourceFileLocations() {
            return Config.FILES_CONFIG.get(configuration);
        }

        @Override
        public String getResourceFilePattern() {
            return Config.FILE_NAME_CONFIG.get(configuration);
        }

        @Override
        public List<String> getValuesFileLocations() {
            return Config.VALUE_FILES_CONFIG.get(configuration);
        }

        @Override
        public String getValuesFilePattern() {
            return Config.VALUE_FILE_NAME_CONFIG.get(configuration);
        }

        @Override
        public NamedValueSet getValues() {
            return NamedValueSet.emptySet();
        }

        @Override
        public NamedValueSet getLabels() {
            return Config.LABEL_CONFIG.getOptional(configuration)
                .map(NamedValueSet::setOf)
                .orElse(NamedValueSet.emptySet());
        }
    }
}
