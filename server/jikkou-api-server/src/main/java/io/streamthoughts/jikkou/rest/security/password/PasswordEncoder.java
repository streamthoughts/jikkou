/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.security.password;

import io.micronaut.core.annotation.NonNull;

/**
 * Service interface for encoding passwords.
 */
public interface PasswordEncoder {

    /**
     * Verify the encoded password obtained from storage matches the submitted raw
     * password after it too is encoded. Returns true if the passwords match, false if
     * they do not. The stored password itself is never decoded.
     *
     * @param rawPassword the raw password to encode and match
     * @param encodedPassword the encoded password from storage to compare with
     * @return true if the raw password, after encoding, matches the encoded password from storage
     */
    boolean matches(
            @NonNull String rawPassword,
            @NonNull String encodedPassword);

    /**
     * Encode the raw password.
     *
     * @param rawPassword   the raw password to encode.
     * @return              the encoded password.
     */
    String encode(@NonNull String rawPassword);
}