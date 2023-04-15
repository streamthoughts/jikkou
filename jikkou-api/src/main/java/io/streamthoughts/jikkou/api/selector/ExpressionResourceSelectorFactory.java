/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.api.selector;

import io.streamthoughts.jikkou.api.error.SelectorException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public final class ExpressionResourceSelectorFactory {

    public static final String FIELD_SELECTOR = "field";
    private final Supplier<ExpressionResourceSelector> defaultSelector;
    private final Map<String, Supplier<ExpressionResourceSelector>> selectors;

    /**
     * Creates a new {@link ExpressionResourceSelectorFactory} instance.
     */
    public ExpressionResourceSelectorFactory() {
        this.defaultSelector = FieldSelector::new;
        this.selectors = new LinkedHashMap<>();
        this.selectors.put(FIELD_SELECTOR, defaultSelector);
    }

    /**
     * Make a {@link ResourceSelector} from the given expression string.
     *
     * @param expressionStrings the expression string.
     * @return  a new {@link ResourceSelector}.
     */
    public List<ResourceSelector> make(@Nullable final String[] expressionStrings) {
        if (expressionStrings == null || expressionStrings.length == 0)
            return Collections.emptyList();

        return make(Arrays.asList(expressionStrings));
    }
    /**
     * Make a {@link ResourceSelector} from the given expression string.
     *
     * @param expressionStrings the expression string.
     * @return  a new {@link ResourceSelector}.
     */
    public List<ResourceSelector> make(@Nullable final List<String> expressionStrings) {
        if (expressionStrings == null || expressionStrings.isEmpty())
            return Collections.emptyList();

        SelectorExpressionParser parser = new SelectorExpressionParser();
        return expressionStrings.stream()
                .flatMap(expressionString -> parser.parseExpressionString(expressionString).stream())
                .map(expression -> {
                    String selectorName = expression.selector();
                    ExpressionResourceSelector selector = findSelectorByName(selectorName);
                    selector.setKey(expression.key());
                    selector.setOperator(expression.operator());
                    selector.setValues(expression.values());
                    return (ResourceSelector) selector;
                })
                .toList();
    }

    private ExpressionResourceSelector findSelectorByName(final String name) {
        if (name == null) return defaultSelector.get();

        return Optional
                .ofNullable(selectors.get(name))
                .map(Supplier::get)
                .orElseThrow(() -> new SelectorException("Failed to find a selector for name '" + name + "'"));
    }
}
