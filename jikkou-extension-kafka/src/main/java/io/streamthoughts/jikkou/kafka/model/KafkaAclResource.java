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
                policy.getResourcePattern(),
                policy.getResourceType(),
                policy.getPatternType(),
                policy.getHost()
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
