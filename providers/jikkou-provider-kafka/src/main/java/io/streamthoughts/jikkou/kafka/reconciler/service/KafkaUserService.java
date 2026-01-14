/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler.service;

import io.streamthoughts.jikkou.common.utils.Encoding;
import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.common.utils.SecurePasswordGenerator;
import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.kafka.model.user.V1KafkaUser;
import io.streamthoughts.jikkou.kafka.model.user.V1KafkaUserAuthentication;
import io.streamthoughts.jikkou.kafka.model.user.V1KafkaUserSpec;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ScramCredentialInfo;
import org.apache.kafka.clients.admin.ScramMechanism;
import org.apache.kafka.clients.admin.UserScramCredentialAlteration;
import org.apache.kafka.clients.admin.UserScramCredentialUpsertion;
import org.apache.kafka.clients.admin.UserScramCredentialsDescription;
import org.jetbrains.annotations.NotNull;

public class KafkaUserService {

    private final AdminClient client;

    public KafkaUserService(final AdminClient client) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
    }

    public Optional<V1KafkaUser> findUserScramCredentials(final String user) {
        try {
            Map<String, UserScramCredentialsDescription> all = client.describeUserScramCredentials(List.of(user))
                .all()
                .get();
            return Optional.ofNullable(all.get(user)).map(description -> toKafkaUser(user, description));
        } catch (InterruptedException e) {
            throw new io.streamthoughts.jikkou.core.exceptions.InterruptedException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new JikkouRuntimeException(cause);
        }
    }

    public List<V1KafkaUser> listUserScramCredentials() {
        try {
            Map<String, UserScramCredentialsDescription> all = client
                .describeUserScramCredentials()
                .all()
                .get();

            return all.entrySet().stream()
                .map(entry -> toKafkaUser(entry.getKey(), entry.getValue()))
                .toList();

        } catch (InterruptedException e) {
            throw new io.streamthoughts.jikkou.core.exceptions.InterruptedException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new JikkouRuntimeException(cause);
        }
    }

    private @NotNull V1KafkaUser toKafkaUser(final String user, final UserScramCredentialsDescription description) {
        return V1KafkaUser.builder()
            .withMetadata(new ObjectMeta(user))
            .withSpec(V1KafkaUserSpec
                .builder()
                .withAuthentications(description.credentialInfos()
                    .stream()
                    .map(this::map)
                    .filter(Objects::nonNull)
                    .toList())
                .build()
            ).build();
    }

    public static Pair<V1KafkaUserAuthentication, UserScramCredentialAlteration> handleScramSha512(String userName,
                                                                                                   V1KafkaUserAuthentication.ScramSha512 auth) {
        var info = new ScramCredentialInfo(
            ScramMechanism.SCRAM_SHA_512,
            Optional.ofNullable(auth.iterations()).orElse(V1KafkaUserAuthentication.DEFAULT_ITERATIONS)
        );

        String password = auth.password();
        if (Strings.isNullOrEmpty(auth.password())) {
            password = SecurePasswordGenerator.getDefault().generate(32);
        }

        var alteration = new UserScramCredentialUpsertion(userName, info, password);
        return Pair.of(
            auth.toBuilder()
                .withIterations(info.iterations())
                .withPassword(password)
                .withSalt(Encoding.BASE64.encode(alteration.salt()))
                .build(),
            alteration
        );
    }

    public static Pair<V1KafkaUserAuthentication, UserScramCredentialAlteration> handleScramSha256(String userName,
                                                                                                   V1KafkaUserAuthentication.ScramSha256 auth) {
        var info = new ScramCredentialInfo(
            ScramMechanism.SCRAM_SHA_256,
            Optional.ofNullable(auth.iterations()).orElse(V1KafkaUserAuthentication.DEFAULT_ITERATIONS)
        );

        String password = auth.password();
        if (Strings.isNullOrEmpty(auth.password())) {
            password = SecurePasswordGenerator.getDefault().generate(32);
        }

        var alteration = new UserScramCredentialUpsertion(userName, info, password);
        return Pair.of(
            auth.toBuilder()
                .withIterations(info.iterations())
                .withPassword(password)
                .withSalt(Encoding.BASE64.encode(alteration.salt()))
                .build(),
            alteration
        );
    }

    private V1KafkaUserAuthentication map(final ScramCredentialInfo info) {
        ScramMechanism mechanism = info.mechanism();
        return switch (mechanism) {
            case UNKNOWN -> null;
            case SCRAM_SHA_256 -> new V1KafkaUserAuthentication.ScramSha256(
                null,
                info.iterations(),
                null
            );
            case SCRAM_SHA_512 -> new V1KafkaUserAuthentication.ScramSha512(
                null,
                info.iterations(),
                null
            );
        };
    }
}
