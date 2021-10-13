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
package io.streamthoughts.kafka.specs;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.streamthoughts.kafka.specs.internal.PropertiesUtils.fromProperties;

public class GlobalSpecsContext {

    private final Map<String, String> systemEnv;

    private final Map<String, String> systemProps;

    private final Map<String, Object> labels;

    /**
     * Creates a new {@link GlobalSpecsContext} instance.
     */
    public GlobalSpecsContext() {
        this(new LinkedHashMap<>());
    }

    /**
     * Creates a new {@link GlobalSpecsContext} instance.
     *
     * @param labels    a list of labels.
     */
    public GlobalSpecsContext(@NotNull final Map<String, Object> labels) {
        this.systemProps = fromProperties(System.getProperties());
        this.systemEnv = System.getenv();
        this.labels = labels;
    }

    public GlobalSpecsContext labels(final Map<String, Object> labels) {
        this.labels.putAll(labels);
        return this;
    }

    public Map<String, Object> getLabels() {
        return explode(labels);
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

    private static Map<String, Object> explode(final Map<String, ?> map) {
        final Stream<Map.Entry<String, ?>> stream = map.entrySet()
                .stream()
                .map(entry -> {
                    if (entry.getKey().contains(".")) {
                        final String[] split = splitPath(entry.getKey());
                        final  Map<String, Object> nested = explode(Collections.singletonMap(split[1], entry.getValue()));
                        return Map.entry(split[0], nested);
                    }
                    else if (entry.getValue() instanceof Map) {
                        return Map.entry(entry.getKey(), explode((Map)entry.getValue()));
                    }
                    return entry;
                });

        return stream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static String[] splitPath(final String key) {
        return key.split("\\.", 2);
    }
}
