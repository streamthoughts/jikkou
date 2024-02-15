/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.security.password;

import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

@Singleton
public class DefaultPasswordEncoder implements PasswordEncoder {

    public static final String DEFAULT_ENCODING_ID = "bcrypt";
    org.springframework.security.crypto.password.PasswordEncoder delegate;

    public DefaultPasswordEncoder() {
        this(DEFAULT_ENCODING_ID);
    }

    @SuppressWarnings("deprecation")
    private DefaultPasswordEncoder(@NonNull String encodingId) {
        Map<String, org.springframework.security.crypto.password.PasswordEncoder> encoders = new HashMap<>();
        encoders.put(encodingId, new BCryptPasswordEncoder());
        encoders.put("noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
        encoders.put("scrypt", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("sha256", new org.springframework.security.crypto.password.StandardPasswordEncoder());
        encoders.put("argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        delegate = new DelegatingPasswordEncoder(encodingId, encoders);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean matches(@NonNull String rawPassword,
                           @NonNull String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String encode(@NonNull String rawPassword) {
        return delegate.encode(rawPassword);
    }
}
