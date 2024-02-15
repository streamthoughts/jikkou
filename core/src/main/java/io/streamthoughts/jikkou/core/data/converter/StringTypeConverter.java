/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.data.converter;

import io.streamthoughts.jikkou.core.data.TypeConverter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class StringTypeConverter implements TypeConverter<String> {

    /**
     * {@inheritDoc}
     **/
    @Override
    public String convertValue(Object value) {
        if (value instanceof ByteBuffer buffer) {
            return StandardCharsets.UTF_8.decode(buffer).toString();
        }
        return (value != null) ? value.toString() : null;
    }
}
