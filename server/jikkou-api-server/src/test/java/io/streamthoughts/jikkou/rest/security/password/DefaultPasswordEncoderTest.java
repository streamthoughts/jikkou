/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.security.password;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultPasswordEncoderTest {

    public static final String RAW_PASSWORD = "test";

    DefaultPasswordEncoder passwordEncoder = new DefaultPasswordEncoder();

    @Test
    void shouldEncodedPassword() {
        String encoded = passwordEncoder.encode(RAW_PASSWORD);
        Assertions.assertNotEquals(RAW_PASSWORD, encoded);
    }

    @Test
    void shouldMatchRPasswordForDefaultEncoding() {
        String encoded = passwordEncoder.encode(RAW_PASSWORD);
        Assertions.assertTrue(passwordEncoder.matches("test", encoded));
    }

    @Test
    void shouldMatchPasswordsForNoopEncoding() {
        Assertions.assertTrue(passwordEncoder.matches("test", "{noop}test"));
    }
}