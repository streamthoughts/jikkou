/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.data.converter;

import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class ByteBufferTypeConverter implements TypeConverter<ByteBuffer> {

    /**
     * {@inheritDoc}
     **/
    @Override
    public ByteBuffer convertValue(Object object) {
        if (object == null) return null;

        if (object instanceof ByteBuffer value) {
            return value;
        }

        if (object instanceof String value) {
            return ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8));
        }

        if (object.getClass().isArray()) {
            return ByteBuffer.wrap((byte[]) object);
        }
        throw new ConfigException(String.format("Cannot parse byte[] from \"%s\"", object));
    }
}
