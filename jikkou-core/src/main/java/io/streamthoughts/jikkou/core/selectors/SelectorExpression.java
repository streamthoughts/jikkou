/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.core.selectors;

import io.streamthoughts.jikkou.core.exceptions.InvalidSelectorException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * SelectorExpression.
 *
 * @param expression the expression string.
 * @param selector   the name of the selector.
 * @param key        the expression key.
 * @param operator   the expression operator.
 * @param values     the expression values.
 */
public record SelectorExpression(@NotNull String expression,
                                 @Nullable String selector,
                                 @NotNull String key,
                                 @NotNull ExpressionOperator operator,
                                 @NotNull List<String> values) {

    /**
     * Creates a new {@link SelectorExpression} instance.
     */
    public SelectorExpression {
        if (operator == ExpressionOperator.INVALID) {
            throw new InvalidSelectorException(
                    "Unknown operator: '"
                            + operator + "' in expression '"
                            + expression + "' Valid are: " + ExpressionOperator.validSet()
            );
        }
    }

    /**
     * Creates a new {@link SelectorExpression} instance.
     *
     * @param expression the expression string.
     * @param selector   the name of the selector.
     * @param key        the expression key.
     * @param operator   the expression operator.
     * @param values     the expression values.
     */
    public SelectorExpression(@NotNull String expression,
                              @Nullable String selector,
                              @NotNull String key,
                              @NotNull String operator,
                              @NotNull List<String> values) {
        this(expression, selector, key, ExpressionOperator.findByName(operator), values);
    }

    public MatchExpression create(ExpressionKeyValueExtractor extractor) {
        return operator().create(key, values, extractor);
    }
}
