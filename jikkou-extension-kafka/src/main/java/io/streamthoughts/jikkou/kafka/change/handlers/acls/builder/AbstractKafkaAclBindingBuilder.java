/*
 * Copyright 2020 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.change.handlers.acls.builder;

import static java.util.Optional.ofNullable;

import io.streamthoughts.jikkou.kafka.change.handlers.acls.KafkaAclBindingBuilder;
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
