/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io.reader;

import io.streamthoughts.jikkou.core.models.NamedValue;
import io.streamthoughts.jikkou.core.models.NamedValueSet;

/**
 * An immutable class to represents options to be passed to a resource reader.
 *
 * @see ResourceReader
 */
public class ResourceReaderOptions {

    public static final String DEFAULT_PATTERN = "**/*.{yaml,yml,tpl}";

    private final String pattern;
    private final NamedValueSet values;
    private final NamedValueSet labels;

    public ResourceReaderOptions() {
        this(NamedValueSet.emptySet(), NamedValueSet.emptySet(), DEFAULT_PATTERN);
    }

    private ResourceReaderOptions(final NamedValueSet values,
                                  final NamedValueSet labels,
                                  final String pattern) {
        this.values = values;
        this.labels = labels;
        this.pattern = pattern;
    }

    public ResourceReaderOptions withValue(final NamedValue value) {
        return new ResourceReaderOptions(values.with(value), labels, pattern);
    }

    public ResourceReaderOptions withLabel(final NamedValue label) {
        return new ResourceReaderOptions(values, labels.with(label), pattern);
    }

    public ResourceReaderOptions withValues(final Iterable<NamedValue> values) {
        return new ResourceReaderOptions(this.values.with(values), labels, pattern);
    }

    public ResourceReaderOptions withLabels(final Iterable<NamedValue> labels) {
        return new ResourceReaderOptions(values, this.labels.with(labels), pattern);
    }

    public ResourceReaderOptions withPattern(final String pattern) {
        return new ResourceReaderOptions(values, labels, pattern);
    }

    public String pattern() {
        return pattern;
    }

    public NamedValueSet values() {
        return values;
    }

    public NamedValueSet labels() {
        return labels;
    }

}
