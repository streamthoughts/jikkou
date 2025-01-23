/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LabelSelector implements Selector {

    private final PreparedExpression preparedExpression;

    /**
     * Creates a new {@link LabelSelector} instance.
     */
    public LabelSelector(final PreparedExpression preparedExpression) {
        this.preparedExpression = preparedExpression;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean apply(@NotNull HasMetadata resource) {
        return preparedExpression.create(new LabelKeyValueExtractor()).apply(resource);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<String> getSelectorExpressions() {
        return List.of(preparedExpression.expression());
    }

    public PreparedExpression preparedExpression() {
        return preparedExpression;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return preparedExpression.toString();
    }

    static class LabelKeyValueExtractor implements ExpressionKeyValueExtractor {

        /** {@inheritDoc} **/
        @Override
        public String getKeyValue(@NotNull HasMetadata resource, @Nullable String key) {
            if (key == null) {
                throw new IllegalArgumentException("Cannot apply extractor with empty key");
            }

            Object value = resource.getMetadata().getLabels().get(key);
            return Optional.ofNullable(value).map(Object::toString).orElse(null);
        }

        /** {@inheritDoc} **/
        @Override
        public boolean isKeyExists(@NotNull HasMetadata resource, @Nullable String key) {
            if (key == null) {
                throw new IllegalArgumentException("Cannot apply extractor with empty key");
            }
            return resource.getMetadata().getLabels().containsKey(key);
        }
    }
}
