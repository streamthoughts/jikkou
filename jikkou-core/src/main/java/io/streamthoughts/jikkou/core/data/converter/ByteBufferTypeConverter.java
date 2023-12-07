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
