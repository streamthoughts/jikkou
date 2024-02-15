/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.security;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.streamthoughts.jikkou.rest.configs.security.BasicAuthCredentials;
import io.streamthoughts.jikkou.rest.configs.security.SecurityConfiguration;
import io.streamthoughts.jikkou.rest.security.password.PasswordEncoder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Singleton
public class AuthenticationProviderUserPassword implements AuthenticationProvider<HttpRequest<?>> {
    @Inject
    private SecurityConfiguration securityProperties;

    @Inject
    private PasswordEncoder encoder;

    @Override
    public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest,
                                                          AuthenticationRequest<?, ?> authenticationRequest) {

        return Flux.create(emitter -> {
            String username = String.valueOf(authenticationRequest.getIdentity());
            Optional<BasicAuthCredentials> optionalBasicAuth = securityProperties.getBasicAuth()
                    .stream()
                    .filter(basicAuth -> basicAuth.username().equals(username))
                    .findFirst();

            // User not found
            if (optionalBasicAuth.isEmpty()) {
                emitter.error(AuthenticationResponse.exception(AuthenticationFailureReason.USER_NOT_FOUND));
            } else {
                BasicAuthCredentials auth = optionalBasicAuth.get();
                String secret = (String) authenticationRequest.getSecret();
                if (encoder.matches(secret, auth.password())) {
                    emitter.next(AuthenticationResponse.success((String) authenticationRequest.getIdentity()));
                    emitter.complete();
                } else {
                    emitter.error(AuthenticationResponse.exception());
                }
            }
        }, FluxSink.OverflowStrategy.ERROR);
    }
}