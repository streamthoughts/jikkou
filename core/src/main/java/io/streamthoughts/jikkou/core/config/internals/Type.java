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
package io.streamthoughts.jikkou.core.config.internals;

import io.streamthoughts.jikkou.core.data.TypeConverter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the convertible type.
 */
public enum Type {

    SHORT(Collections.singletonList(Short.class), TypeConverter.Short()),
    INTEGER(Collections.singletonList(Integer.class), TypeConverter.Integer()),
    LONG(Collections.singletonList(Long.class), TypeConverter.Long()),
    FLOAT(Collections.singletonList(Float.class), TypeConverter.Float()),
    DOUBLE(Collections.singletonList(Double.class), TypeConverter.Double()),
    BOOLEAN(Collections.singletonList(Boolean.class), TypeConverter.Boolean()),
    STRING(Collections.singletonList(String.class), TypeConverter.String()),
    LIST(List.of(Collection.class, List.class, Set.class), TypeConverter.ofList(Object.class)),
    MAP(List.of(Map.class), TypeConverter.ofMap()),
    BYTES(Collections.emptyList(), TypeConverter.Bytes());

    private final static Map<Class<?>, Type> JAVA_CLASS_TYPES = new HashMap<>();

    static {
        for (Type type : Type.values()) {
            for (Class<?> typeClass : type.classes) {
                JAVA_CLASS_TYPES.put(typeClass, type);
            }
        }
    }

    private final Collection<Class<?>> classes;
    private final TypeConverter<?> converter;

    /**
     * Creates a new {@link Type} instance.
     */
    Type(final Collection<Class<?>> classes,
         final TypeConverter<?> converter) {
        this.classes = Objects.requireNonNull(classes);
        this.converter = Objects.requireNonNull(converter);
    }

    @SuppressWarnings("unchecked")
    public <T> TypeConverter<T> converter() {
        return (TypeConverter<T>) converter;
    }

    public static Type forClass(final Class<?> cls) {
        synchronized (JAVA_CLASS_TYPES) {
            Type type = JAVA_CLASS_TYPES.get(cls);
            if (type != null)
                return type;

            // Since the lookup only checks the class, we need to also try
            for (Map.Entry<Class<?>, Type> entry : JAVA_CLASS_TYPES.entrySet()) {
                try {
                    cls.asSubclass(entry.getKey());
                    // Cache this for subsequent lookups
                    JAVA_CLASS_TYPES.put(cls, entry.getValue());
                    return entry.getValue();
                } catch (ClassCastException e) {
                    // Expected, ignore
                }
            }
        }
        return null;
    }
}