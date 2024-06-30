/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

import java.security.SecureRandom;

/**
 * Service interface for generating secure passwords.
 */
public interface SecurePasswordGenerator {

    /**
     * Generates a random secure password of the given length.
     *
     * @param length    The length of the password to generate.
     * @return  a secure random generated password.
     */
    String generate(int length);

    static SecurePasswordGenerator getDefault() {
        return new DefaultSecurePasswordGenerator();
    }

    /**
     * Default implementation for {@link SecurePasswordGenerator}.
     */
    class DefaultSecurePasswordGenerator implements SecurePasswordGenerator {
        // Character sets to use in generated passwords
        private static final String LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
        private static final String UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private static final String DIGITS = "0123456789";
        private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+?/~";
        private static final String ALL_CHARACTERS = LOWERCASE_LETTERS + UPPERCASE_LETTERS + DIGITS + SPECIAL_CHARACTERS;

        /**
         * {@inheritDoc}
         */
        @Override
        public String generate(int length) {
            SecureRandom random = new SecureRandom();
            StringBuilder password = new StringBuilder(length);

            // Ensure the password has at least one character from each character set
            password.append(LOWERCASE_LETTERS.charAt(random.nextInt(LOWERCASE_LETTERS.length())));
            password.append(UPPERCASE_LETTERS.charAt(random.nextInt(UPPERCASE_LETTERS.length())));
            password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
            password.append(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));

            // Fill the rest of the password length with random characters from all character sets
            for (int i = 4; i < length; i++) {
                password.append(ALL_CHARACTERS.charAt(random.nextInt(ALL_CHARACTERS.length())));
            }

            // Shuffle the characters to ensure randomness
            for (int i = password.length() - 1; i > 0; i--) {
                int index = random.nextInt(i + 1);
                char temp = password.charAt(index);
                password.setCharAt(index, password.charAt(i));
                password.setCharAt(i, temp);
            }
            return password.toString();
        }
    }
}
