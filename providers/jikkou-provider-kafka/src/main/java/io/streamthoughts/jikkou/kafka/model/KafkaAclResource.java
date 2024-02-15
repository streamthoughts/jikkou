/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.model;

import java.util.Objects;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

/**
 * This class can be used to group {@link KafkaAclBinding} by resource.
 */
public final class KafkaAclResource {

    private final String pattern;
    private final ResourceType resourceType;
    private final PatternType patternType;
    private final String host;

    /**
     * Creates a new {@link KafkaAclResource} from the given {@link KafkaAclBinding}.
     * @param policy    the {@link KafkaAclBinding}.
     */
    public KafkaAclResource(final KafkaAclBinding policy) {
        this(
                policy.resourcePattern(),
                policy.resourceType(),
                policy.patternType(),
                policy.host()
        );
    }

    /**
     * Creates a new {@link KafkaAclResource} instance.
     *
     * @param pattern      the resource pattern.
     * @param resourceType the resource type.
     * @param patternType  the pattern type.
     * @param host         the host.
     */
    private KafkaAclResource(final String pattern,
                             final ResourceType resourceType,
                             final PatternType patternType,
                             final String host) {
        this.pattern = pattern;
        this.resourceType = resourceType;
        this.patternType = patternType;
        this.host = host;
    }

    public String pattern() {
        return pattern;
    }

    public ResourceType resourceType() {
        return resourceType;
    }

    public PatternType patternType() {
        return patternType;
    }

    public String host() {
        return host;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaAclResource that = (KafkaAclResource) o;
        return Objects.equals(pattern, that.pattern) && resourceType == that.resourceType && patternType == that.patternType && Objects.equals(host, that.host);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(pattern, resourceType, patternType, host);
    }
}
