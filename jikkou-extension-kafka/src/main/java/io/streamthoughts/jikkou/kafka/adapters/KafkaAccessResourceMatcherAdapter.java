/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.adapters;

import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessResourceMatcher;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;

public final class KafkaAccessResourceMatcherAdapter {

    public static KafkaAccessResourceMatcherAdapter from(final @NotNull V1KafkaAccessResourceMatcher matcher) {
        return new KafkaAccessResourceMatcherAdapter(
                matcher.getPattern(),
                matcher.getPatternType(),
                matcher.getType()
        );
    }

    private static final String WILDCARD = "*";
    private static final Pattern LITERAL = Pattern.compile("[a-zA-Z0-9\\._\\-]+");

    private final String pattern;
    private final PatternType patternType;
    private final ResourceType type;

    public KafkaAccessResourceMatcherAdapter(final @NotNull String pattern,
                                             final PatternType patternType,
                                             final  @NotNull ResourceType type) {
        this.pattern = pattern;
        this.patternType = patternType == null ? PatternType.LITERAL : patternType;
        this.type = type;
        validate();
    }

    private void validate() {
        if (PatternType.LITERAL.equals(patternType) && type.equals(ResourceType.TOPIC)) {
            if ( !(LITERAL.matcher(pattern).matches() || pattern.equals(WILDCARD)) ) {
                throw new IllegalArgumentException("This literal pattern for topic resource is not supported: " + pattern);
            }
        }
    }

    public boolean isPatternOfTypeMatchRegex() {
        return this.patternType == PatternType.MATCH
                && this.pattern.startsWith("/")
                && this.pattern.endsWith("/");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KafkaAccessResourceMatcherAdapter)) return false;
        KafkaAccessResourceMatcherAdapter that = (KafkaAccessResourceMatcherAdapter) o;
        return Objects.equals(pattern, that.pattern) &&
                patternType == that.patternType &&
                type == that.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(pattern, patternType, type);
    }
}
