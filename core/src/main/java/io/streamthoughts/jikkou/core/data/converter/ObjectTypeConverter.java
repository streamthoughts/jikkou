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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import java.util.List;
import java.util.Objects;

public final class ObjectTypeConverter<T> implements TypeConverter<T> {

    private static final ObjectMapper DEFAULT_OBJECT_OBJECT = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .build();

    private final ObjectMapper objectMapper;
    private final JavaType type;

    /**
     * Creates a new converter for converting object into the given type.
     *
     * @param objectType Type of object.
     * @param <T>        Type of object.
     * @return The converter.
     */
    public static <T> ObjectTypeConverter<T> newForType(final TypeReference<T> objectType) {
        TypeFactory typeFactory = DEFAULT_OBJECT_OBJECT.getTypeFactory();
        JavaType type = typeFactory.constructType(objectType);
        return new ObjectTypeConverter<>(DEFAULT_OBJECT_OBJECT, type);
    }

    /**
     * Creates a new converter for converting object into the given type.
     *
     * @param objectType Type of object.
     * @param <T>        Type of object.
     * @return The converter.
     */
    public static <T> ObjectTypeConverter<T> newForType(final Class<T> objectType) {
        TypeFactory typeFactory = DEFAULT_OBJECT_OBJECT.getTypeFactory();
        JavaType type = typeFactory.constructType(objectType);
        return new ObjectTypeConverter<>(DEFAULT_OBJECT_OBJECT, type);
    }

    /**
     * Creates a new converter for converting object into a list of elements of the given type.
     *
     * @param elementClass Type of elements.
     * @param <T>          Type of object.
     * @return The converter.
     */
    public static <T> ObjectTypeConverter<List<T>> newForList(Class<T> elementClass) {
        TypeFactory typeFactory = DEFAULT_OBJECT_OBJECT.getTypeFactory();
        CollectionType type = typeFactory.constructCollectionType(List.class, elementClass);
        return new ObjectTypeConverter<>(DEFAULT_OBJECT_OBJECT, type);
    }

    /**
     * Creates a new {@link ObjectTypeConverter} instance.
     *
     * @param objectMapper The {@link ObjectMapper}.
     */
    public ObjectTypeConverter(ObjectMapper objectMapper, JavaType type) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public T convertValue(Object value) {
        return value == null ? null : objectMapper.convertValue(value, type);
    }
}
