/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.acl;

import static java.util.function.Predicate.not;

import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Change;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final class AclChangeComputer
        extends ResourceChangeComputer<String, V1KafkaPrincipalAuthorization> {

    public static final String ACL = "acl";

    /**
     * Creates a new {@link AclChangeComputer} instance.
     *
     * @param kafkaAclBindingBuilder the {@link KafkaAclBindingBuilder}.
     */
    public AclChangeComputer(boolean isDeleteBindingForOrphanPrincipal,
                             @NotNull KafkaAclBindingBuilder kafkaAclBindingBuilder) {
        super(
                object -> object.getMetadata().getName(),
                new AclChangeFactory(kafkaAclBindingBuilder),
                isDeleteBindingForOrphanPrincipal
        );
    }

    public static class AclChangeFactory extends ResourceChangeFactory<String, V1KafkaPrincipalAuthorization> {

        private final KafkaAclBindingBuilder kafkaAclBindingBuilder;

        public AclChangeFactory(final KafkaAclBindingBuilder kafkaAclBindingBuilder) {
            this.kafkaAclBindingBuilder = kafkaAclBindingBuilder;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public ResourceChange createChangeForDelete(final String key, final V1KafkaPrincipalAuthorization before) {
            List<KafkaAclBinding> bindings = kafkaAclBindingBuilder.toKafkaAclBindings(before);
            List<StateChange> changes = bindings.stream()
                    .map(binding -> StateChange.delete(ACL, binding))
                    .collect(Collectors.toList());

            return GenericResourceChange
                    .builder(V1KafkaPrincipalAuthorization.class)
                    .withMetadata(before.getMetadata())
                    .withSpec(ResourceChangeSpec
                            .builder()
                            .withOperation(Operation.DELETE)
                            .withChanges(changes)
                            .build()
                    )
                    .build();
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public ResourceChange createChangeForCreate(final String key, final V1KafkaPrincipalAuthorization after) {
            List<KafkaAclBinding> bindings = kafkaAclBindingBuilder.toKafkaAclBindings(after);

            List<StateChange> changes = bindings.stream()
                    .map(binding -> StateChange.create(ACL, binding))
                    .collect(Collectors.toList());

            return GenericResourceChange
                    .builder(V1KafkaPrincipalAuthorization.class)
                    .withMetadata(after.getMetadata())
                    .withSpec(ResourceChangeSpec
                            .builder()
                            .withOperation(Operation.CREATE)
                            .withChanges(changes)
                            .build()
                    )
                    .build();
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public ResourceChange createChangeForUpdate(final String key,
                                                    final V1KafkaPrincipalAuthorization before,
                                                    final V1KafkaPrincipalAuthorization after) {
            List<KafkaAclBinding> beforeBindings = kafkaAclBindingBuilder.toKafkaAclBindings(before);
            List<KafkaAclBinding> afterBindings = kafkaAclBindingBuilder.toKafkaAclBindings(after);

            List<StateChange> changes = new ArrayList<>();
            // Compute NONE
            afterBindings.stream()
                    .filter(not(KafkaAclBinding::isDeleted))
                    .filter(beforeBindings::contains)
                    .map(binding -> StateChange.none(ACL, binding))
                    .forEach(changes::add);

            // Compute ADD
            afterBindings.stream()
                    .filter(not(KafkaAclBinding::isDeleted))
                    .filter(not(beforeBindings::contains))
                    .map(binding -> StateChange.create(ACL, binding))
                    .forEach(changes::add);

            // Compute DELETE (for explicit deletions)
            afterBindings.stream()
                    .filter(beforeBindings::contains)
                    .filter(KafkaAclBinding::isDeleted)
                    .map(binding -> StateChange.delete(ACL, binding))
                    .forEach(changes::add);

            // Compute DELETE (for orphans ACL bindings)
            beforeBindings.stream()
                    .filter(not(afterBindings::contains))
                    .map(binding -> StateChange.delete(ACL, binding))
                    .forEach(changes::add);

            return GenericResourceChange
                    .builder(V1KafkaPrincipalAuthorization.class)
                    .withMetadata(after.getMetadata())
                    .withSpec(ResourceChangeSpec
                            .builder()
                            .withOperation(Change.computeOperation(changes))
                            .withChanges(changes)
                            .build()
                    )
                    .build();
        }
    }
}
