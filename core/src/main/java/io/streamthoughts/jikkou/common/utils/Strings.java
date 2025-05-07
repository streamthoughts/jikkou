/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * Utilities for manipulating string.
 */
public final class Strings {

    private Strings() {
    }

    public static boolean isBlank(final String string) {
        return string == null || string.trim().isEmpty();

    }

    public static boolean isNotBlank(final String string) {
        return !isBlank(string);
    }

    public static Properties toProperties(String string) {
        final String formattedString = string.trim().replace(",", System.lineSeparator());
        try (StringReader stringReader = new StringReader(formattedString)) {
            final Properties properties = new Properties();
            properties.load(stringReader);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String pruneSuffix(final String s, final String suffix) {
        int pos = s.lastIndexOf(suffix);
        if (pos > 0) {
            return s.substring(0, pos);
        }
        return s;
    }

    public static String prunePrefix(final String s, final String prefix) {
        return s.replaceFirst(prefix, "");
    }
}