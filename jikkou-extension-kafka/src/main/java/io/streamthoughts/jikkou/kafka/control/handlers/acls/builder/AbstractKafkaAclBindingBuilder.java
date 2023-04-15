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

import io.streamthoughts.jikkou.kafka.control.handlers.acls.KafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAcl;
import io.streamthoughts.jikkou.kafka.models.V1KafkaResourceMatcher;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

abstract class AbstractKafkaAclBindingBuilder implements KafkaAclBindingBuilder {


    List<KafkaAclBinding> buildAclBindings(final String principal,
                                           final boolean delete,
                                           final Collection<V1KafkaPrincipalAcl> permissions) {
        return buildAclBindings(
                principal,
                delete,
                permissions,
                null,
                null,
                null
        );
    }

    List<KafkaAclBinding> buildAclBindings(final String principal,
                                           final boolean delete,
                                           final Collection<V1KafkaPrincipalAcl> permissions,
                                           final String overrideResourcePattern,
                                           final PatternType overridePatternType,
                                           final ResourceType overrideResourceType
    ) {

        List<KafkaAclBinding> bindings = new LinkedList<>();
        for (V1KafkaPrincipalAcl permission : permissions) {
            for (AclOperation operation : permission.getOperations()) {
                V1KafkaResourceMatcher resource = permission.getResource();
                KafkaAclBinding binding = KafkaAclBinding.builder()
                        .withPrincipal(principal)
                        .withResourcePattern(overrideResourcePattern == null ? resource.getPattern() : overrideResourcePattern)
                        .withPatternType(overridePatternType == null ? resource.getPatternType() : overridePatternType)
                        .withResourceType(overrideResourceType == null ? resource.getType() : overrideResourceType)
                        .withType(permission.getType())
                        .withOperation(operation)
                        .withHost(permission.getHost())
                        .withDelete(delete)
                        .build();
                bindings.add(binding);
            }
        }
        return bindings;
    }
}
