/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.control.change;

import static java.util.function.Predicate.not;

import io.streamthoughts.jikkou.api.control.ChangeComputer;
import io.streamthoughts.jikkou.kafka.control.handlers.acls.KafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;

public final class AclChangeComputer implements ChangeComputer<V1KafkaPrincipalAuthorization, AclChange> {

    private final KafkaAclBindingBuilder kafkaAclBindingBuilder;

    private boolean isDeleteBindingForOrphanPrincipal = false;

    /**
     * Creates a new {@link AclChangeComputer} instance.
     *
     * @param kafkaAclBindingBuilder the {@link KafkaAclBindingBuilder}.
     */
    public AclChangeComputer(@NotNull KafkaAclBindingBuilder kafkaAclBindingBuilder) {
        this.kafkaAclBindingBuilder = Objects.requireNonNull(
                kafkaAclBindingBuilder,
                "kafkaAclBindingBuilder must not be null"
        );
    }

    private Map<String, List<KafkaAclBinding>> toKafkaAclBindingsByPrincipal(Iterable<V1KafkaPrincipalAuthorization> resources) {
        return StreamSupport.stream(resources.spliterator(), false)
                .flatMap(resource -> kafkaAclBindingBuilder.toKafkaAclBindings(resource).stream())
                .collect(Collectors.groupingBy(KafkaAclBinding::getPrincipal));
    }

    public void setDeleteAclBindingForOrphanPrincipal(final boolean isDeleteBindingForOrphanPrincipal) {
        this.isDeleteBindingForOrphanPrincipal = isDeleteBindingForOrphanPrincipal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AclChange> computeChanges(@NotNull final Iterable<V1KafkaPrincipalAuthorization> actualResources,
                                          @NotNull final Iterable<V1KafkaPrincipalAuthorization> expectedResources) {
        Map<String, List<KafkaAclBinding>> actualBindingsGroupedByPrincipal =
                toKafkaAclBindingsByPrincipal(actualResources);

        Map<String, List<KafkaAclBinding>> expectBindingsGroupedByPrincipal =
                toKafkaAclBindingsByPrincipal(expectedResources);

        final Map<String, List<AclChange>> results = new HashMap<>();

        for (Map.Entry<String, List<KafkaAclBinding>> entry : expectBindingsGroupedByPrincipal.entrySet()) {

            String principalName = entry.getKey();

            List<KafkaAclBinding> expectPrincipalBindings = entry.getValue();
            List<KafkaAclBinding> actualPrincipalBindings = actualBindingsGroupedByPrincipal.get(principalName);

            List<AclChange> principalChanges = new LinkedList<>();

            if (actualPrincipalBindings == null) {
                // Compute ADD
                expectPrincipalBindings
                        .stream()
                        .filter(not(KafkaAclBinding::isDelete))
                        .map(AclChange::add)
                        .forEach(principalChanges::add);
            } else {
                // Compute NONE
                expectPrincipalBindings.stream()
                        .filter(not(KafkaAclBinding::isDelete))
                        .filter(actualPrincipalBindings::contains)
                        .map(AclChange::none)
                        .forEach(principalChanges::add);

                // Compute ADD
                expectPrincipalBindings.stream()
                        .filter(not(KafkaAclBinding::isDelete))
                        .filter(not(actualPrincipalBindings::contains))
                        .map(AclChange::add)
                        .forEach(principalChanges::add);

                // Compute DELETE (for explicit deletions)
                expectPrincipalBindings.stream()
                        .filter(actualPrincipalBindings::contains)
                        .filter(KafkaAclBinding::isDelete)
                        .map(AclChange::delete)
                        .forEach(principalChanges::add);

                // Compute DELETE (for orphans ACL bindings)
                actualPrincipalBindings.stream()
                        .filter(not(expectPrincipalBindings::contains))
                        .map(AclChange::delete)
                        .forEach(principalChanges::add);
            }
            results.put(principalName, principalChanges);
        }

        if (isDeleteBindingForOrphanPrincipal) {
            // Compute DELETE (for orphans Principal)
            Map<String, List<AclChange>> deleteChangesForOrphanPrincipals = actualBindingsGroupedByPrincipal
                    .entrySet()
                    .stream()
                    .filter(not(entry -> results.containsKey(entry.getKey())))
                    .map(Map.Entry::getValue)
                    .flatMap(kafkaAclBindings -> kafkaAclBindings.stream().map(AclChange::delete))
                    .collect(Collectors.groupingBy(aclChange -> aclChange.getAclBindings().getPrincipal()));
            results.putAll(deleteChangesForOrphanPrincipals);
        }

        return results.values()
                .stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(it -> it.getAclBindings().getPrincipal()))
                .toList();
    }
}
