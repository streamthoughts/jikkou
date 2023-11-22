/*
 * Copyright 2023 The original authors
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
