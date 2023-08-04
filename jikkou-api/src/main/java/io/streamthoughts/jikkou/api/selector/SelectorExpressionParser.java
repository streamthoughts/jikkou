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
package io.streamthoughts.jikkou.api.selector;

import io.streamthoughts.jikkou.api.error.InvalidSelectorException;
import io.streamthoughts.jikkou.common.utils.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public final class SelectorExpressionParser {

    private static final Pattern CONDITION_PATTERN = Pattern.compile(
            "(?<key>[a-zA-Z0-9\\._-]+)\\s+(?<operator>\\w+)\\s*(?<values>\\(.*?\\)|\\S+)?");
    private static final Pattern VALUES_SPLIT_PATTERN =  Pattern.compile("\\s*,\\s*");

    private static final Pattern VALUES_PARENTHESIS_PATTERN = Pattern.compile("[\\(\\)]");

    public List<SelectorExpression> parseExpressionString(@NotNull String expressionString) {

        if (Strings.isBlank(expressionString)) {
            throw new InvalidSelectorException("Cannot parse empty or blank expression string");
        }

        // Parse the selector
        String[] parts = expressionString.split(":");

        String selector = null;
        String conditionsString = expressionString;
        if (parts.length == 2) {
            selector = parts[0].trim();
            conditionsString = parts[1].trim();
        }

        Matcher conditionMatcher = CONDITION_PATTERN.matcher(conditionsString);
        List<SelectorExpression> expressions = new ArrayList<>();
        while (conditionMatcher.find()) {
            String key = conditionMatcher.group("key");
            String operator = conditionMatcher.group("operator");
            String valuesString = conditionMatcher.group("values");
            if (valuesString != null) {
                valuesString = VALUES_PARENTHESIS_PATTERN.matcher(valuesString).replaceAll("");
            }
            List<String> values = new ArrayList<>();
            if (valuesString != null && !valuesString.isEmpty()) {
                values.addAll(Arrays.stream(VALUES_SPLIT_PATTERN.split(valuesString)).map(String::trim).toList());
            }
            var expr = new SelectorExpression(
                    expressionString,
                    selector,
                    key,
                    operator,
                    values
            );
            expressions.add(expr);
        }

        if (expressions.isEmpty()) {
            throw new InvalidSelectorException("Failed to parse selectors from expression string: " + expressionString);
        }
        return expressions;
    }
}