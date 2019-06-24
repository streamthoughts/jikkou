/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.kafka.specs.acl;

import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Represents a binding between resources pattern and a set of allowed operations.
 */
public class AclResourcePermission {

    private static final String WILDCARD = "*";
    private static final Pattern LITERAL = Pattern.compile("[a-zA-Z0-9\\._\\-]+");

    private final String pattern;
    private final PatternType patternType;
    private final Set<AclOperationPolicy> operations;
    private final ResourceType type;

    /**
     * Creates a new {@link AclResourcePermission} instance.
     *
     * @param pattern       apply operations to all resources matching specified pattern.
     * @param operations    list operations to allowed.
     */
    AclResourcePermission(final String pattern,
                          final PatternType patternType,
                          final ResourceType type,
                          final Set<AclOperationPolicy> operations) {
        Objects.requireNonNull(pattern, "pattern cannot be null");
        Objects.requireNonNull(patternType, "patternType cannot be null");
        Objects.requireNonNull(operations, "operations cannot be null");
        validateForTopicAndLiteralPattern(pattern, patternType, type);

        this.type = type;
        this.pattern = pattern;
        this.patternType = patternType;
        this.operations = operations;
    }

    private void validateForTopicAndLiteralPattern(final String pattern,
                                                   final PatternType patternType,
                                                   final ResourceType type) {
        if (PatternType.LITERAL.equals(patternType) && type.equals(ResourceType.TOPIC)) {
            if ( !(LITERAL.matcher(pattern).matches() || pattern.equals(WILDCARD)) ) {
                throw new IllegalArgumentException("This literal pattern for topic resource is not supported: " + pattern);
            }
        }
    }

    public ResourceType getType() {
        return type;
    }

    public String pattern() {
        return pattern;
    }

    public PatternType patternType() {
        return patternType;
    }

    public boolean isPatternOfTypeMatchRegex() {
        return this.patternType == PatternType.MATCH
                && this.pattern.startsWith("/")
                && this.pattern.endsWith("/");
    }

    public Set<AclOperationPolicy> operations() {
        return operations;
    }

    public String[] operationLiterals() {
      return this.operations
                .stream()
                .map(AclOperationPolicy::toLiteral)
                .toArray(String[]::new);
    }

    @Override
    public String toString() {
        return "AclResourcePermission{" +
                "pattern=" + pattern +
                ", patternType=" + patternType +
                ", operations=" + operations +
                '}';
    }
}
