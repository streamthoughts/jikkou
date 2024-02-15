/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.models.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum ExpressionOperator {

    /**
     * Select only {@link Resource} having
     * selector key IN the specified list of values.
     */
    IN {
        @Override
        public MatchExpression create(String key, List<String> values, ExpressionKeyValueExtractor extractor) {
            return resource -> {
                String value = extractor.getKeyValue(resource, key);
                return value != null && values.contains(value);
            };
        }
    },

    /**
     * Select only {@link Resource} having
     * selector key NOT IN the specified list of values.
     */
    NOTIN {
        @Override
        public MatchExpression create(String key, List<String> values, ExpressionKeyValueExtractor extractor) {
            return resource -> {
                String value = extractor.getKeyValue(resource, key);
                return value != null && !values.contains(value);
            };
        }
    },

    /**
     * Select only {@link Resource} having
     * the EXISTING selector key.
     */
    EXISTS {
        @Override
        public MatchExpression create(String key, List<String> values, ExpressionKeyValueExtractor extractor) {
            return resource -> extractor.isKeyExists(resource, key);
        }
    },

    /**
     * Select only {@link Resource} having no
     * EXISTING selector key.
     */
    DOESNOTEXISTS {
        @Override
        public MatchExpression create(String key, List<String> values, ExpressionKeyValueExtractor extractor) {
            return resource -> !extractor.isKeyExists(resource, key);
        }
    },


    /**
     * Select only {@link Resource} having
     * a selector key that match one of the specified regex.
     */
    MATCHES {
        @Override
        public MatchExpression create(final String key,
                                      final List<String> values,
                                      final ExpressionKeyValueExtractor extractor) {
            return resource -> {
                String value = extractor.getKeyValue(resource, key);
                if (value == null) return false;
                return values.stream()
                        .map(Pattern::compile)
                        .map(pattern -> pattern.matcher(value))
                        .allMatch(Matcher::matches);
            };
        }
    },

    /**
     * Select only {@link Resource} having
     * a selector key that do not match any of the specified regex.
     */
    DOESNOTMATCH {
        @Override
        public MatchExpression create(String key, List<String> values, ExpressionKeyValueExtractor extractor) {
            return resource -> {
                String value = extractor.getKeyValue(resource, key);
                if (value == null) return true;
                return values.stream()
                        .map(Pattern::compile)
                        .map(pattern -> pattern.matcher(value))
                        .noneMatch(Matcher::matches);
            };
        }
    },

    /**
     * Invalid/Unsupported expression operator.
     */
    INVALID;

    private static final Map<String, ExpressionOperator> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(
            a -> a.name().toLowerCase(),
            a -> a));

    public static ExpressionOperator findByName(String name) {
        return Optional.ofNullable(BY_NAME.get(name.toLowerCase())).orElse(INVALID);
    }

    public MatchExpression create(final String key,
                                  final List<String> values,
                                  final ExpressionKeyValueExtractor extractor) {
        throw new UnsupportedOperationException();
    }

    public static Set<String> validSet() {
        return Arrays.stream(values()).filter(e -> e != INVALID).map(Enum::name).collect(Collectors.toSet());
    }
}
