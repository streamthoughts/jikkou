/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.user;

import io.streamthoughts.jikkou.common.utils.Classes;
import io.streamthoughts.jikkou.common.utils.SecurePasswordGenerator;
import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Change;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.change.ChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ChangeComputerBuilder;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.streamthoughts.jikkou.kafka.KafkaLabelAndAnnotations;
import io.streamthoughts.jikkou.kafka.model.user.V1KafkaUser;
import io.streamthoughts.jikkou.kafka.model.user.V1KafkaUserAuthentication;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * ResourceChangeComputer for {@link V1KafkaUser}.
 */
public final class UserChangeComputer extends ResourceChangeComputer<String, V1KafkaUser> {

    public static final String AUTHENTICATIONS_CHANGE_PREFIX = "authentications.";

    public UserChangeComputer() {
        super(ChangeComputerBuilder.KeyMapper.byName(), new UserChangeFactory(), false);
    }

    public static final class UserChangeFactory extends ResourceChangeFactory<String, V1KafkaUser> {

        /**
         * {@inheritDoc}
         */
        @Override
        public ResourceChange createChangeForCreate(final String key, final V1KafkaUser after) {
            List<V1KafkaUserAuthentication> authentications = after.getSpec().authentications();

            // Compute change for 'authentications'
            List<SpecificStateChange<V1KafkaUserAuthentication>> changes = authentications
                .stream()
                .map(it ->
                    StateChange.create(
                        getUserAuthenticationChangeName(it),
                        getUserAuthenticationWithDefaults(it)
                    )
                )
                .toList();

            return GenericResourceChange
                .builder(V1KafkaUser.class)
                .withMetadata(after.getMetadata())
                .withSpec(ResourceChangeSpec
                    .builder()
                    .withOperation(Operation.CREATE)
                    .withChanges(changes)
                    .build()
                )
                .build();
        }

        private static @NotNull String getUserAuthenticationChangeName(V1KafkaUserAuthentication it) {
            return AUTHENTICATIONS_CHANGE_PREFIX + Classes.toKebabCase(it.getClass());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ResourceChange createChangeForUpdate(final String key, final V1KafkaUser before, final V1KafkaUser after) {

            boolean forcePasswordRenewal = after.getMetadata()
                .findAnnotationByKey(KafkaLabelAndAnnotations.JIKKOU_IO_KAFKA_FORCE_PASSWORD_RENEWAL)
                .map(TypeConverter.Boolean()::convertValue)
                .orElse(false);

            final Map<String, V1KafkaUserAuthentication> beforeAuthenticationsByKey = before.getSpec().authentications()
                .stream()
                .collect(Collectors.toMap(UserChangeFactory::getUserAuthenticationChangeName, Function.identity()));

            final Map<String, V1KafkaUserAuthentication> afterAuthenticationsByKey = after.getSpec().authentications()
                .stream()
                .collect(Collectors.toMap(UserChangeFactory::getUserAuthenticationChangeName, Function.identity()));

            List<StateChange> stateChanges = ChangeComputer.computeChanges(
                beforeAuthenticationsByKey,
                afterAuthenticationsByKey,
                true,
                (o1, o2) -> !forcePasswordRenewal
            );

            stateChanges = stateChanges.stream()
                .map(change -> {
                    Operation op = change.getOp();
                    if (op.equals(Operation.DELETE) || op.equals(Operation.NONE)) {
                        return change;
                    }
                    V1KafkaUserAuthentication authentication = (V1KafkaUserAuthentication) change.getAfter();
                    authentication = getUserAuthenticationWithDefaults(authentication);
                    return new SpecificStateChange<>(
                        change.getName(),
                        change.getOp(),
                        change.getBefore(),
                        authentication,
                        change.getDescription()
                    );
                })
                .toList();

            return GenericResourceChange
                .builder(V1KafkaUser.class)
                .withMetadata(after.getMetadata())
                .withSpec(ResourceChangeSpec
                    .builder()
                    .withOperation(Change.computeOperation(stateChanges))
                    .withChanges(stateChanges)
                    .build()
                )
                .build();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ResourceChange createChangeForDelete(final String key, final V1KafkaUser before) {
            List<V1KafkaUserAuthentication> authentications = before.getSpec().authentications();

            List<SpecificStateChange<V1KafkaUserAuthentication>> changes = authentications
                .stream()
                .map(it -> StateChange.delete(getUserAuthenticationChangeName(it), it))
                .toList();

            return GenericResourceChange
                .builder(V1KafkaUser.class)
                .withMetadata(before.getMetadata())
                .withSpec(ResourceChangeSpec
                    .builder()
                    .withOperation(Operation.DELETE)
                    .withChanges(changes)
                    .build()
                )
                .build();
        }

        private @NotNull V1KafkaUserAuthentication getUserAuthenticationWithDefaults(final V1KafkaUserAuthentication authentication) {
            return switch (authentication) {
                case V1KafkaUserAuthentication.ScramSha256 scramSha256 -> {
                    yield new V1KafkaUserAuthentication.ScramSha256(
                        Strings.isBlank(scramSha256.password()) ? SecurePasswordGenerator.getDefault().generate(32) : scramSha256.password(),
                        Optional.ofNullable(scramSha256.iterations()).orElse(V1KafkaUserAuthentication.DEFAULT_ITERATIONS),
                        scramSha256.salt()
                    );
                }
                case V1KafkaUserAuthentication.ScramSha512 scramSha512 -> {
                    yield new V1KafkaUserAuthentication.ScramSha512(
                        Strings.isBlank(scramSha512.password()) ? SecurePasswordGenerator.getDefault().generate(32) : scramSha512.password(),
                        Optional.ofNullable(scramSha512.iterations()).orElse(V1KafkaUserAuthentication.DEFAULT_ITERATIONS),
                        scramSha512.salt()
                    );
                }
            };
        }
    }
}
