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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
        toNestedMap(labels, this.labels, null);
        toFlattenMap(labels, this.labels, null);
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

    @VisibleForTesting
    static void toNestedMap(final Map<String, Object> source,
                            final Map<String, Object> result,
                            final String key) {

        source.forEach((k, v) -> {
            if (k.contains(".")) {
                String[] parts = k.split("\\.", 2);
                k = parts[0];
                v = Map.of(parts[1], v);
            }

            final String path = key == null ? k : key + "." + k;

            if (v instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) v;
                Map<String, Object> nestedMap;
                if (result.containsKey(k)) {
                    Object nested = result.get(k);
                    if (nested instanceof Map) {
                        // create a new Map to make sure it is not immutable
                        nestedMap = new HashMap<>((Map<String, Object>) nested);
                    } else {
                        throw new IllegalArgumentException("Duplicate key: " + path);
                    }
                } else {
                    nestedMap = new HashMap<>();
                }
                toNestedMap(mapValue, nestedMap, path);
                result.put(k, nestedMap);
                return;
            }

            if (result.containsKey(k)) {
                throw new IllegalArgumentException("Duplicate key: " + path);
            }
            result.put(k, v);
        });
    }

    @SuppressWarnings("unchecked")
    private static void toFlattenMap(final Map<String, Object> source,
                                     final Map<String, Object> result,
                                     final String key) {
        source.forEach((k, v) -> {
            final String currKey = key == null ? k : key + '.' + k;
            if (v instanceof Map) {
                toFlattenMap((Map<String, Object>) v, result, currKey);
            } else if (v instanceof List) {
                final List<Object> list = (List<Object>) v;
                for (int i = 0, size = list.size(); i < size; i++) {
                    result.put(currKey + '[' + (i + 1) + ']', list.get(i));
                }
            } else {
                result.put(currKey, v);
            }
        });
    }
}
