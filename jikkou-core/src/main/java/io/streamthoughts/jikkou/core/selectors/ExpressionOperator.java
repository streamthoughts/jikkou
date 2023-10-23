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
