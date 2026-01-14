/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.api.template;

import static java.util.stream.Collectors.toCollection;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.loader.CascadingResourceLocator;
import com.hubspot.jinjava.loader.ClasspathResourceLocator;
import com.hubspot.jinjava.loader.FileLocator;
import com.hubspot.jinjava.loader.ResourceLocator;
import com.hubspot.jinjava.mode.ExecutionMode;
import io.streamthoughts.jikkou.common.utils.CollectionUtils;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.template.ResourceTemplateRenderer;
import io.streamthoughts.jikkou.core.template.TemplateBindings;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Jinja based template rendered.
 */
public class JinjaResourceTemplateRenderer implements ResourceTemplateRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(JinjaResourceTemplateRenderer.class);

    private static final String CONFIG_NS = "jinja";

    public static final ConfigProperty<Boolean> ENABLE_RECURSIVE_MACRO_CALLS = ConfigProperty
        .ofBoolean(CONFIG_NS + ".enableRecursiveMacroCalls")
        .defaultValue(true)
        .description("Enable recursive macro calls.");

    public static final ConfigProperty<List<String>> RESOURCE_LOCATIONS_CALLS = ConfigProperty
        .ofList(CONFIG_NS + ".resourceLocations")
        .required(false)
        .description("File locations from where you want to allow Jinja to load files.");

    // list of scopes for bindings
    public enum Scopes {
        LABELS, VALUES, SYSTEM, ENV, PROPS, RESOURCE;

        public String key() {
            return name().toLowerCase();
        }
    }

    private boolean failOnUnknownTokens = true;

    private boolean preserveRawTags = false;

    public JinjaResourceTemplateRenderer withFailOnUnknownTokens(final boolean failOnUnknownTokens) {
        this.failOnUnknownTokens = failOnUnknownTokens;
        return this;
    }

    public JinjaResourceTemplateRenderer withPreserveRawTags(final boolean preserveRawTags) {
        this.preserveRawTags = preserveRawTags;
        return this;
    }

    private Configuration configuration = Configuration.empty();

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(final @NotNull Configuration config) throws ConfigException {
        this.configuration = config;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String render(@NotNull final String template,
                         @Nullable final URI location,
                         @NotNull final TemplateBindings bindings) {
        LOG.debug("Starting resource template rendering");
        JinjavaConfig config = JinjavaConfig.newBuilder()
            .withCharset(StandardCharsets.UTF_8)
            .withFailOnUnknownTokens(failOnUnknownTokens)
            .withExecutionMode(new PreserveRawExecutionMode(preserveRawTags))
            .withEnableRecursiveMacroCalls(ENABLE_RECURSIVE_MACRO_CALLS.get(configuration))
            .build();

        Jinjava jinjava = new Jinjava(config);

        List<String> locations = RESOURCE_LOCATIONS_CALLS.getOptional(configuration).orElse(List.of());
        if (!locations.isEmpty()) {
            List<ResourceLocator> locators = locations.stream().map(baseDir -> {
                try {
                    return (ResourceLocator) new FileLocator(new File(baseDir));
                } catch (FileNotFoundException e) {
                    LOG.warn("Cannot configure Jinja resource location: '{}'. Cause: {}.", baseDir, e.getMessage());
                    return null;
                }
            }).filter(Objects::nonNull).collect(toCollection(ArrayList::new));
            locators.add(new ClasspathResourceLocator());
            jinjava.setResourceLocator(new CascadingResourceLocator(locators.toArray(new ResourceLocator[0])));
        }
        Map<String, Object> bindingsMap = buildBindingsMapFrom(bindings, location);
        RenderResult result = jinjava.renderForResult(template, bindingsMap);

        List<TemplateError> errors = result.getErrors();
        if (!errors.isEmpty()) {
            TemplateError error = errors.getFirst();
            throw new JikkouRuntimeException(
                String.format(
                    "Cannot render resource template. '%s': line %d, start_pos: %d, %s",
                    formatErrorReason(error.getReason().name()),
                    error.getLineno(),
                    error.getStartPosition(),
                    error.getMessage()
                )
            );
        }
        LOG.debug("Resource template rendering done");
        return result.getOutput();
    }

    @NotNull
    @VisibleForTesting
    static Map<String, Object> buildBindingsMapFrom(final TemplateBindings bindings,
                                                    final URI location) {
        HashMap<String, Object> bindingsMap = new HashMap<>();

        bindingsMap.put(Scopes.VALUES.key(), bindings.getValues());

        Map<String, Object> labels = new HashMap<>();
        CollectionUtils.toNestedMap(bindings.getLabels(), labels, null);
        CollectionUtils.toFlattenMap(bindings.getLabels(), labels, null);
        bindingsMap.put(Scopes.LABELS.key(), labels);

        Map<String, Map<String, Object>> systemValues = new HashMap<>();
        systemValues.put(Scopes.ENV.key(), bindings.getSystemEnv());
        systemValues.put(Scopes.PROPS.key(), bindings.getSystemProps());

        bindingsMap.put(Scopes.SYSTEM.key(), systemValues);

        if (location != null) {
            Path path = Paths.get(location.getPath());
            bindingsMap.put(Scopes.RESOURCE.key(), Map.of(
                    "path", path,
                    "name", path.getFileName().toString(),
                    "directoryPath", Optional.ofNullable(path.getParent()).map(Path::toAbsolutePath).map(Path::toString).orElse("")
                )
            );
        }

        return bindingsMap;
    }

    /**
     * @return the input string to camel-case.
     */
    private static String formatErrorReason(final String s) {
        String formatted = Pattern.compile("_([a-z])")
            .matcher(s.toLowerCase())
            .replaceAll(m -> m.group(1).toUpperCase());
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }

    static class PreserveRawExecutionMode implements ExecutionMode {
        private final boolean preserveRaw;

        public PreserveRawExecutionMode(final boolean preserveRaw) {
            this.preserveRaw = preserveRaw;
        }

        @Override
        public boolean isPreserveRawTags() {
            return preserveRaw;
        }
    }
}
