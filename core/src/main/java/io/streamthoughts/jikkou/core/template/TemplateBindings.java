/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.template;

import static io.streamthoughts.jikkou.common.utils.PropertiesUtils.toMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Represents objects to put into scope for rendering template.
 */
public final class TemplateBindings {

    private final Map<String, Object> systemEnv;
    private final Map<String, Object> systemProps;
    private final Map<String, Object> values;
    private final Map<String, Object> labels;

    public static TemplateBindings defaults() {
        return new TemplateBindings(
                new HashMap<>(System.getenv()),
                toMap(System.getProperties()),
                new LinkedHashMap<>(),
                new LinkedHashMap<>()
        );
    }

    private TemplateBindings(@NotNull final Map<String, Object> systemEnv,
                             @NotNull final Map<String, Object> systemProps,
                             @NotNull final Map<String, Object> values,
                             @NotNull final Map<String, Object> labels) {
        this.systemEnv = systemEnv;
        this.systemProps = systemProps;
        this.labels = labels;
        this.values = values;
    }

    public TemplateBindings addLabels(@NotNull final Map<String, Object> newLabels) {
        var merged = new HashMap<String, Object>();
        merged.putAll(labels);
        merged.putAll(newLabels);
        return new TemplateBindings(systemEnv, systemProps, values, labels);
    }

    public TemplateBindings addValues(@NotNull final Map<String, Object> newValues) {
        var merged = new HashMap<String, Object>();
        merged.putAll(values);
        merged.putAll(newValues);
        return new TemplateBindings(systemEnv, systemProps, merged, labels);
    }

    public Map<String, Object> getLabels() {
        return this.labels;
    }

    public Map<String, Object> getSystemEnv() {
        return systemEnv;
    }

    public Map<String, Object> getSystemProps() {
        return systemProps;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "[" +
                ", systemEnv=" + systemEnv +
                ", systemProps=" + systemProps +
                ", labels=" + labels +
                ", values=" + values +
                ']';
    }

}
