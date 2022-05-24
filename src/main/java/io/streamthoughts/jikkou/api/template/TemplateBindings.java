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

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.streamthoughts.jikkou.internal.PropertiesUtils.toMap;

/**
 * Represents objects to put into scope for rendering template.
 *
 * @see TemplateRenderer
 */
public class TemplateBindings {

    private final Map<String, String> systemEnv;

    private final Map<String, String> systemProps;

    private final Map<String, Object> values;

    private final Map<String, Object> labels;

    public static TemplateBindings defaults() {
        return new TemplateBindings(
                System.getenv(),
                toMap(System.getProperties()),
                new LinkedHashMap<>(),
                new LinkedHashMap<>()
        );
    }

    private TemplateBindings(@NotNull final Map<String, String> systemEnv,
                             @NotNull final Map<String, String> systemProps,
                             @NotNull final Map<String, Object> values,
                             @NotNull final Map<String, Object> labels) {
        this.systemEnv = systemEnv;
        this.systemProps = systemProps;
        this.labels = labels;
        this.values = values;
    }

    public TemplateBindings withLabels(@NotNull final Map<String, Object> labels) {
        return new TemplateBindings(systemEnv, systemProps, values, labels);
    }

    public TemplateBindings withValues(@NotNull final Map<String, Object> vars) {
        return new TemplateBindings(systemEnv, systemProps, vars, labels);
    }

    public Map<String, Object> getLabels() {
        return this.labels;
    }

    public Map<String, String> getSystemEnv() {
        return systemEnv;
    }

    public Map<String, String> getSystemProps() {
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
