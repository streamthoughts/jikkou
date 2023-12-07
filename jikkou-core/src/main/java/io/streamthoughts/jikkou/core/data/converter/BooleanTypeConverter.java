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
