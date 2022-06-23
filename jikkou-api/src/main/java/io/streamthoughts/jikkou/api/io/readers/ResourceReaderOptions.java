/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.api.io.readers;

import io.streamthoughts.jikkou.api.model.NamedValue;

/**
 * An immutable class to represents options to be passed to a resource reader.
 *
 * @see io.streamthoughts.jikkou.api.io.ResourceReader
 */
public class ResourceReaderOptions {

    public static final String DEFAULT_PATTERN = "**/*.{yaml,yml,tpl}";

    private final String pattern;
    private final NamedValue.Set values;
    private final NamedValue.Set labels;

    private final boolean isTemplatingEnable;

    public ResourceReaderOptions() {
        this(NamedValue.emptySet(), NamedValue.emptySet(), DEFAULT_PATTERN, true);
    }

    private ResourceReaderOptions(final NamedValue.Set values,
                                  final NamedValue.Set labels,
                                  final String pattern,
                                  final boolean isTemplatingEnable) {
        this.values = values;
        this.labels = labels;
        this.pattern = pattern;
        this.isTemplatingEnable = isTemplatingEnable;
    }

    public ResourceReaderOptions withValue(final NamedValue value) {
        return new ResourceReaderOptions(values.with(value), labels, pattern, isTemplatingEnable);
    }

    public ResourceReaderOptions withLabel(final NamedValue label) {
        return new ResourceReaderOptions(values, labels.with(label), pattern, isTemplatingEnable);
    }

    public ResourceReaderOptions withValues(final Iterable<NamedValue> values) {
        return new ResourceReaderOptions(this.values.with(values), labels, pattern, isTemplatingEnable);
    }

    public ResourceReaderOptions withLabels(final Iterable<NamedValue> labels) {
        return new ResourceReaderOptions(values, this.labels.with(labels), pattern, isTemplatingEnable);
    }

    public ResourceReaderOptions withPattern(final String pattern) {
        return new ResourceReaderOptions(values, labels, pattern, isTemplatingEnable);
    }

    public ResourceReaderOptions withTemplatingEnable(final boolean isTemplatingEnable) {
        return new ResourceReaderOptions(values, labels, pattern, isTemplatingEnable);
    }

    public String pattern() {
        return pattern;
    }

    public NamedValue.Set values() {
        return values;
    }

    public NamedValue.Set labels() {
        return labels;
    }

    public boolean isTemplatingEnable() {
        return isTemplatingEnable;
    }
}
