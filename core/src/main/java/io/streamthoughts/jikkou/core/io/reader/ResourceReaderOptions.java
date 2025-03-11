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
public record ResourceReaderOptions(
    String pattern,
    NamedValueSet values,
    NamedValueSet labels
) {
    public static final String DEFAULT_PATTERN = "**/*.{yaml,yml,tpl}";

    public static final ResourceReaderOptions DEFAULTS = new ResourceReaderOptions(
        DEFAULT_PATTERN,
        NamedValueSet.emptySet(),
        NamedValueSet.emptySet()
    );

    public ResourceReaderOptions withValue(final NamedValue value) {
        return new ResourceReaderOptions(pattern, values.with(value), labels);
    }

    public ResourceReaderOptions withLabel(final NamedValue label) {
        return new ResourceReaderOptions(pattern, values, labels.with(label));
    }

    public ResourceReaderOptions withValues(final Iterable<NamedValue> values) {
        return new ResourceReaderOptions(pattern, this.values.with(values), labels);
    }

    public ResourceReaderOptions withLabels(final Iterable<NamedValue> labels) {
        return new ResourceReaderOptions(pattern, values, this.labels.with(labels));
    }

    public ResourceReaderOptions withPattern(final String pattern) {
        return new ResourceReaderOptions(pattern, values, labels);
    }
}
