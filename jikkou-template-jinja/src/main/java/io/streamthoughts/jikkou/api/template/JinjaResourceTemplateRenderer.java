/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.api.template;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.mode.ExecutionMode;
import io.streamthoughts.jikkou.api.error.JikkouException;
import io.streamthoughts.jikkou.common.utils.CollectionUtils;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * A Jinja based template rendered.
 */
public class JinjaResourceTemplateRenderer implements ResourceTemplateRenderer {

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

    /** {@inheritDoc} **/
    @Override
    public String render(@NotNull final String template,
                         @NotNull final TemplateBindings bindings) {

        JinjavaConfig config = JinjavaConfig.newBuilder()
                .withCharset(StandardCharsets.UTF_8)
                .withFailOnUnknownTokens(failOnUnknownTokens)
                .withExecutionMode(new PreserveRawExecutionMode(preserveRawTags))
                .build();

        Jinjava jinjava = new Jinjava(config);
        Map<String, Object> bindingsMap = buildBindingsMapFrom(bindings);
        RenderResult result = jinjava.renderForResult(template, bindingsMap);

        List<TemplateError> errors = result.getErrors();
        if (!errors.isEmpty()) {
            TemplateError error = errors.get(0);
            throw new JikkouException(
                    String.format(
                            "%s: line %d, %s",
                            formatErrorReason(error.getReason().name()),
                            error.getLineno(),
                            error.getMessage())
            );
        }

        return result.getOutput();
    }

    @NotNull
    @VisibleForTesting
    static Map<String, Object> buildBindingsMapFrom(final TemplateBindings bindings) {
        HashMap<String, Object> bindingsMap = new HashMap<>();

        Map<String, Object> values = new HashMap<>();
        CollectionUtils.toNestedMap(bindings.getValues(), values, null);
        CollectionUtils.toFlattenMap(bindings.getValues(), values, null);
        bindingsMap.put(Scopes.VALUES.key(), values);

        Map<String, Object> labels = new HashMap<>();
        CollectionUtils.toNestedMap(bindings.getLabels(), labels, null);
        CollectionUtils.toFlattenMap(bindings.getLabels(), labels, null);
        bindingsMap.put(Scopes.LABELS.key(), labels);

        Map<String, Map<String, String>> systemValues = Map.of(
                Scopes.ENV.key(), bindings.getSystemEnv(),
                Scopes.PROPS.key(), bindings.getSystemProps()
        );
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
