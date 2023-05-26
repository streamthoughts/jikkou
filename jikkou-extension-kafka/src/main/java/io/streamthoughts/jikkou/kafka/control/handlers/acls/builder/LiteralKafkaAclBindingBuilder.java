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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.kafka.common.resource.PatternType;

public class LiteralKafkaAclBindingBuilder extends AbstractKafkaAclBindingBuilder implements KafkaAclBindingBuilder {

    /**
     * Creates a new {@link LiteralKafkaAclBindingBuilder} instance.
     */
    public LiteralKafkaAclBindingBuilder() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<KafkaAclBinding> toKafkaAclBindings(final V1KafkaPrincipalAuthorization resource) {

        List<V1KafkaPrincipalAcl> acls = getAcceptedAclsFromResource(resource);
        if (acls.isEmpty()) return Collections.emptyList();

        String principal = getPrincipalFromResource(resource);
        return buildAclBindings(
                principal,
                acls,
                null,
                null,
                null,
                JikkouMetadataAnnotations.isAnnotatedWithDelete(resource)
        );
    }

    private List<V1KafkaPrincipalAcl> getAcceptedAclsFromResource(V1KafkaPrincipalAuthorization resource) {
        return resource.getSpec().getAcls()
                .stream()
                .filter(this::isAclAccepted)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isAclAccepted(V1KafkaPrincipalAcl object) {
        return object.getResource().getPatternType() != PatternType.MATCH;
    }

    private String getPrincipalFromResource(V1KafkaPrincipalAuthorization resource) {
        return resource.getMetadata().getName();
    }
}
