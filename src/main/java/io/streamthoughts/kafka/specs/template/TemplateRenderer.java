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
package io.streamthoughts.kafka.specs.template;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import io.streamthoughts.kafka.specs.error.KafkaSpecsException;
import io.streamthoughts.kafka.specs.internal.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for manipulating template using Jinja.
 */
public class TemplateRenderer {

    // list of scopes for bindings
    public static final String SCOPE_LABELS             = "labels";
    public static final String SCOPE_VARS               = "vars";
    public static final String SCOPE_SYSTEM             = "system";
    public static final String SCOPE_SYSTEM_ENV         = "env";
    public static final String SCOPE_SYSTEM_PROPS       = "props";

    public static String compile(@NotNull final String template,
                                 @NotNull final TemplateBindings bindings) {

        JinjavaConfig config = JinjavaConfig.newBuilder()
                .withCharset(StandardCharsets.UTF_8)
                .withFailOnUnknownTokens(true)
                .build();

        Jinjava jinjava = new Jinjava(config);

        Map<String, Object> bindingsMap = buildBindingsMapFrom(bindings);

        RenderResult result = jinjava.renderForResult(template, bindingsMap);

        List<TemplateError> errors = result.getErrors();
        if (!errors.isEmpty()) {
            TemplateError error = errors.get(0);
            throw new KafkaSpecsException(
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

        Map<String, Object> vars = new HashMap<>();
        CollectionUtils.toNestedMap(bindings.getVars(), vars, null);
        CollectionUtils.toFlattenMap(bindings.getVars(), vars, null);
        bindingsMap.put(SCOPE_VARS, vars);

        Map<String, Object> labels = new HashMap<>();
        CollectionUtils.toNestedMap(bindings.getLabels(), labels, null);
        CollectionUtils.toFlattenMap(bindings.getLabels(), labels, null);
        bindingsMap.put(SCOPE_LABELS, labels);

        bindingsMap.put(SCOPE_SYSTEM, Map.of(
                SCOPE_SYSTEM_ENV, bindings.getSystemEnv(),
                SCOPE_SYSTEM_PROPS, bindings.getSystemProps())
        );
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
}
