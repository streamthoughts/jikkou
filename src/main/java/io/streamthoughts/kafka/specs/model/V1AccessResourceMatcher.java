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
package io.streamthoughts.kafka.specs.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.Objects;
import java.util.regex.Pattern;

public class V1AccessResourceMatcher {

    private static final String WILDCARD = "*";
    private static final Pattern LITERAL = Pattern.compile("[a-zA-Z0-9\\._\\-]+");

    private final String pattern;
    private final PatternType patternType;
    private final ResourceType type;

    @JsonCreator
    public V1AccessResourceMatcher(final @JsonProperty("pattern") String pattern,
                                   final @JsonProperty("pattern_type") PatternType patternType,
                                   final @JsonProperty("type") ResourceType type) {
        this.pattern = pattern;
        this.patternType = patternType;
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

    @JsonProperty("pattern")
    public String pattern() {
        return pattern;
    }

    @JsonProperty("pattern_type")
    public PatternType patternType() {
        return patternType;
    }

    @JsonProperty("type")
    public ResourceType type() {
        return type;
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
        if (!(o instanceof V1AccessResourceMatcher)) return false;
        V1AccessResourceMatcher that = (V1AccessResourceMatcher) o;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "V1AccessResourceMatcher{" +
                "pattern='" + pattern + '\'' +
                ", patternType=" + patternType +
                ", type=" + type +
                '}';
    }
}
