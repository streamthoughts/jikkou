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
import java.util.Objects;

public final class SelectorExpression {
    private final String expression;
    private final String selector;
    private final String key;
    private final ExpressionOperator operator;
    private final List<String> values;

    /**
     * Creates a new {@link SelectorExpression} instance.
     *
     * @param expression    the expression string.
     * @param selector      the name of the selector.
     * @param key           the expression key.
     * @param operator      the expression operator.
     * @param values        the expression values.
     */
    public SelectorExpression(String expression,
                              String selector,
                              String key,
                              String operator,
                              List<String> values) {
        this.expression = expression;
        this.selector = selector;
        this.key = key;
        this.operator = ExpressionOperator.findByName(operator);
        this.values = values;

        if (this.operator == ExpressionOperator.INVALID) {
            throw new InvalidSelectorException(
                    "Unknown operator: '"
                            + operator + "' in expression '"
                            + expression + "' Valid are: " + ExpressionOperator.validSet()
            );
        }
    }

    public String selector() {
        return selector;
    }

    public String key() {
        return key;
    }

    public ExpressionOperator operator() {
        return operator;
    }

    public List<String> values() {
        return values;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SelectorExpression) obj;
        return Objects.equals(this.expression, that.expression) &&
                Objects.equals(this.selector, that.selector) &&
                Objects.equals(this.key, that.key) &&
                Objects.equals(this.operator, that.operator) &&
                Objects.equals(this.values, that.values);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(expression, selector, key, operator, values);
    }

    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return expression;
    }

}
