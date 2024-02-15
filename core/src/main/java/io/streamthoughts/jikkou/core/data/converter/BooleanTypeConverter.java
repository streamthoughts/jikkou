/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.data.converter;

import io.streamthoughts.jikkou.core.data.TypeConverter;

public final class BooleanTypeConverter implements TypeConverter<Boolean> {

    private static final String BOOLEAN_TRUE = "true";

    /**
     * {@inheritDoc}
     **/
    @Override
    public Boolean convertValue(Object value) {
        if (value == null) return null;

        Boolean result = null;

        if (value instanceof String s) {
            if (s.length() == 1 && Character.isDigit(s.charAt(0))) {
                int digit = s.charAt(0);
                result = digit > 0;
            } else {
                result = s.equalsIgnoreCase(BOOLEAN_TRUE) ||
                        s.equalsIgnoreCase("yes") ||
                        s.equalsIgnoreCase("y");
            }
        }
        if (value instanceof Boolean booleanValue) {
            result = booleanValue;
        }

        if (result == null) {
            throw new IllegalArgumentException(String.format("Cannot parse boolean content from \"%s\"", value));
        }
        return result;
    }
}
