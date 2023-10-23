/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.kafka.change;

import static java.util.function.Predicate.not;

import io.streamthoughts.jikkou.core.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.kafka.change.handlers.acls.KafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AclChangeComputer
        extends ResourceChangeComputer<V1KafkaPrincipalAuthorization, List<KafkaAclBinding>, AclChange> {

    /**
     * Creates a new {@link AclChangeComputer} instance.
     *
     * @param kafkaAclBindingBuilder the {@link KafkaAclBindingBuilder}.
     */
    public AclChangeComputer(boolean isDeleteBindingForOrphanPrincipal,
                             @NotNull KafkaAclBindingBuilder kafkaAclBindingBuilder) {
        super(metadataNameKeyMapper(), new ValueMapper(kafkaAclBindingBuilder), isDeleteBindingForOrphanPrincipal);
    }

    static class ValueMapper implements ChangeValueMapper<V1KafkaPrincipalAuthorization, List<KafkaAclBinding>> {

        private final KafkaAclBindingBuilder aclBindingBuilder;

        public ValueMapper(KafkaAclBindingBuilder kafkaAclBindingBuilder) {
            this.aclBindingBuilder = kafkaAclBindingBuilder;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public @NotNull List<KafkaAclBinding> apply(@Nullable V1KafkaPrincipalAuthorization before,
                                                    @Nullable V1KafkaPrincipalAuthorization after) {
            if (after != null)
                return aclBindingBuilder.toKafkaAclBindings(after);
            if (before != null) {
                return aclBindingBuilder.toKafkaAclBindings(before);
            }
            throw new IllegalArgumentException("both arguments are null");
        }
    }

    /** {@inheritDoc} **/
    @Override
    public List<AclChange> buildChangeForUpdating(List<KafkaAclBinding> before,
                                                  List<KafkaAclBinding> after) {
        List<AclChange> changes = new ArrayList<>();
        // Compute NONE
        after.stream()
            .filter(not(KafkaAclBinding::isDeleted))
            .filter(before::contains)
            .map(AclChange::none)
            .forEach(changes::add);

        // Compute ADD
        after.stream()
            .filter(not(KafkaAclBinding::isDeleted))
            .filter(not(before::contains))
            .map(AclChange::add)
            .forEach(changes::add);

        // Compute DELETE (for explicit deletions)
        after.stream()
            .filter(before::contains)
            .filter(KafkaAclBinding::isDeleted)
            .map(AclChange::delete)
            .forEach(changes::add);

        // Compute DELETE (for orphans ACL bindings)
        before.stream()
            .filter(not(after::contains))
            .map(AclChange::delete)
            .forEach(changes::add);

        return changes;
    }

    /** {@inheritDoc} **/
    @Override
    public List<AclChange> buildChangeForNone(List<KafkaAclBinding> before, List<KafkaAclBinding> after) {
        return after.stream().map(AclChange::none).collect(Collectors.toList());
    }

    /** {@inheritDoc} **/
    @Override
    public List<AclChange> buildChangeForCreating(List<KafkaAclBinding> after) {
        return after.stream().map(AclChange::add).collect(Collectors.toList());
    }

    /** {@inheritDoc} **/
    @Override
    public List<AclChange> buildChangeForDeleting(List<KafkaAclBinding> before) {
        return before.stream().map(AclChange::delete).collect(Collectors.toList());
    }
}
