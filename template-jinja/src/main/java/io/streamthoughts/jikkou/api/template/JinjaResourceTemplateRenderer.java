/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.api.template;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.mode.ExecutionMode;
import io.streamthoughts.jikkou.common.utils.CollectionUtils;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.template.ResourceTemplateRenderer;
import io.streamthoughts.jikkou.core.template.TemplateBindings;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
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
            .orElse(true)
            .description("Enable recursive macro calls.");

    // list of scopes for bindings
    public enum Scopes {
        LABELS, VALUES, SYSTEM, ENV, PROPS;

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
                         @NotNull final TemplateBindings bindings) {
        LOG.debug("Starting resource template rendering");
        JinjavaConfig config = JinjavaConfig.newBuilder()
                .withCharset(StandardCharsets.UTF_8)
                .withFailOnUnknownTokens(failOnUnknownTokens)
                .withExecutionMode(new PreserveRawExecutionMode(preserveRawTags))
                .withEnableRecursiveMacroCalls(ENABLE_RECURSIVE_MACRO_CALLS.get(configuration))
                .build();

        Jinjava jinjava = new Jinjava(config);
        Map<String, Object> bindingsMap = buildBindingsMapFrom(bindings);
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
    static Map<String, Object> buildBindingsMapFrom(final TemplateBindings bindings) {
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
