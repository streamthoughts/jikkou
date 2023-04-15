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
package io.streamthoughts.jikkou.kafka.control.handlers.acls.builder;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.kafka.control.handlers.acls.KafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAcl;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

public class LiteralKafkaAclBindingBuilder extends AbstractKafkaAclBindingBuilder implements KafkaAclBindingBuilder {

    /**
     * Creates a new {@link LiteralKafkaAclBindingBuilder} instance.
     */
    public LiteralKafkaAclBindingBuilder() { }

    /** {@inheritDoc} */
    @Override
    public List<KafkaAclBinding> toKafkaAclBindings(final V1KafkaPrincipalAuthorization resource) {

        boolean annotatedWithDelete = JikkouMetadataAnnotations.isAnnotatedWithDelete(resource);

        List<V1KafkaPrincipalAcl> permissions = resource.getSpec().getAcls()
                .stream()
                .filter(it -> it.getResource().getPatternType() != PatternType.MATCH)
                .distinct()
                .collect(Collectors.toList());

        return buildAclBindings(resource.getMetadata().getName(), annotatedWithDelete, permissions);
    }

    public static class ResourcePattern {

        public final String pattern;
        public final ResourceType resourceType;
        public final PatternType patternType;

        public final String host;

        public ResourcePattern(final KafkaAclBinding policy) {
            this(policy.getResourcePattern(), policy.getResourceType(), policy.getPatternType(), policy.getHost());
        }

        public ResourcePattern(final String pattern,
                               final ResourceType resourceType,
                               final PatternType patternType,
                               final String host) {
            this.pattern = pattern;
            this.resourceType = resourceType;
            this.patternType = patternType;
            this.host = host;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResourcePattern that = (ResourcePattern) o;
            return Objects.equals(pattern, that.pattern)
                    && resourceType == that.resourceType
                    && patternType == that.patternType
                    && Objects.equals(host, that.host);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pattern, resourceType, patternType, host);
        }
    }

}
