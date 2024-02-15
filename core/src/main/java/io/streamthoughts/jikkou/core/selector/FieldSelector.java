/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.selector.internal.PropertyAccessors;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Named("field")
public final class FieldSelector extends ExpressionSelector {

    /**
     * Creates a new {@link FieldSelector} instance.
     */
    public FieldSelector(SelectorExpression expression) {
        super(expression, new PathExpressionKeyValueExtractor());
    }

    static class PathExpressionKeyValueExtractor implements ExpressionKeyValueExtractor {

        /** {@inheritDoc} **/
        @Override
        public String getKeyValue(@NotNull HasMetadata resource, @Nullable String key) {
            if (key == null) {
                throw new IllegalArgumentException("Cannot apply extractor with empty key");
            }
            Object value = new PropertyAccessors().readPropertyValue(resource, key);
            return Optional.ofNullable(value).map(Object::toString).orElse(null);
        }

        /** {@inheritDoc} **/
        @Override
        public boolean isKeyExists(@NotNull HasMetadata resource, @Nullable String key) {
            if (key == null) {
                throw new IllegalArgumentException("Cannot apply extractor with empty key");
            }
            return getKeyValue(resource, key) != null;
        }
    }
}
