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

import static java.util.Optional.ofNullable;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractKafkaAclBindingBuilder implements KafkaAclBindingBuilder {


    List<KafkaAclBinding> buildAclBindings(final String principal,
                                           final Collection<V1KafkaPrincipalAcl> permissions,
                                           final String overrideResourcePattern,
                                           final PatternType overridePatternType,
                                           final ResourceType overrideResourceType,
                                           final boolean delete
    ) {
        List<KafkaAclBinding> bindings = new LinkedList<>();
        for (V1KafkaPrincipalAcl permission : permissions) {
            for (AclOperation operation : permission.getOperations()) {
                KafkaAclBinding binding = getKafkaAclBinding(
                        principal,
                        permission,
                        operation,
                        overrideResourcePattern,
                        overridePatternType,
                        overrideResourceType,
                        delete);
                bindings.add(binding);
            }
        }
        return bindings;
    }

    /**
     * Static helper method to create a new {@link KafkaAclBinding} from the given arguments.
     *
     * @param principal           the principal.
     * @param acl                 the ACL.
     * @param operation           the ACL operation.
     * @param resourcePattern     the resource pattern to be used instead of the one from ACL - can be null.
     * @param resourcePatternType the resource pattern-type to be used instead of the one from ACL - can be null.
     * @param resourceType        the resource type to be used instead of the one from ACL - can be null.
     * @param delete              the flag to indicate if the bind should be deleted.
     * @return  a new {@link KafkaAclBinding}.
     */
    private static KafkaAclBinding getKafkaAclBinding(@NotNull String principal,
                                                      @NotNull V1KafkaPrincipalAcl acl,
                                                      @NotNull AclOperation operation,
                                                      @Nullable String resourcePattern,
                                                      @Nullable PatternType resourcePatternType,
                                                      @Nullable ResourceType resourceType,
                                                      boolean delete) {
        V1KafkaResourceMatcher resource = acl.getResource();

        return KafkaAclBinding.builder()
                .withPrincipal(principal)
                .withResourcePattern(ofNullable(resourcePattern).orElse(resource.getPattern()))
                .withPatternType(ofNullable(resourcePatternType).orElse(resource.getPatternType()))
                .withResourceType(ofNullable(resourceType).orElse(resource.getType()))
                .withType(acl.getType())
                .withOperation(operation)
                .withHost(acl.getHost())
                .withDelete(delete)
                .build();
    }
}
