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

import io.streamthoughts.kafka.specs.internal.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.streamthoughts.kafka.specs.internal.PropertiesUtils.fromProperties;

/**
 * Represents objects to put into scope for rendering template.
 *
 * @see TemplateRenderer
 */
public class TemplateBindings {

    private final Map<String, String> systemEnv;

    private final Map<String, String> systemProps;

    private final Map<String, Object> labels;

    public static TemplateBindings withLabels(final Map<String, Object> labels) {
        return new TemplateBindings(labels);
    }

    /**
     * Creates a new {@link TemplateBindings} instance.
     */
    public TemplateBindings() {
        this(new LinkedHashMap<>());
    }

    /**
     * Creates a new {@link TemplateBindings} instance.
     *
     * @param labels a list of labels.
     */
    public TemplateBindings(@NotNull final Map<String, Object> labels) {
        this.systemProps = fromProperties(System.getProperties());
        this.systemEnv = System.getenv();
        this.labels = new HashMap<>();
        CollectionUtils.toNestedMap(labels, this.labels, null);
        CollectionUtils.toFlattenMap(labels, this.labels, null);
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

    @Override
    public String toString() {
        return "[" +
                ", systemEnv=" + systemEnv +
                ", systemProps=" + systemProps +
                ", labels=" + labels +
                ']';
    }

}
