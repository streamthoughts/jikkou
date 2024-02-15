/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.acl.builder;

import static java.util.Optional.ofNullable;

import io.streamthoughts.jikkou.kafka.change.acl.KafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAcl;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

abstract class AbstractKafkaAclBindingBuilder implements KafkaAclBindingBuilder {


    List<KafkaAclBinding> buildAclBindings(final String principal,
                                           final Collection<V1KafkaPrincipalAcl> acls,
                                           final String overrideResourcePattern,
                                           final PatternType overridePatternType,
                                           final ResourceType overrideResourceType,
                                           final boolean delete
    ) {
        List<KafkaAclBinding> bindings = new LinkedList<>();
        for (V1KafkaPrincipalAcl acl : acls) {
            for (AclOperation operation : acl.getOperations()) {
                KafkaAclBinding binding = new KafkaAclBinding(
                        principal,
                        ofNullable(overrideResourcePattern).orElse(acl.getResource().getPattern()),
                        ofNullable(overridePatternType).orElse(acl.getResource().getPatternType()),
                        ofNullable(overrideResourceType).orElse(acl.getResource().getType()),
                        operation,
                        acl.getType(),
                        acl.getHost(),
                        delete
                );
                bindings.add(binding);
            }
        }
        return bindings;
    }
}
