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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the convertible type.
 */
public enum Type {

    // This is a special type used to deal with NULL object.
    NULL(null) {
        @Override
        public Short convert(@Nullable final Object o) {
            throw new UnsupportedOperationException("Cannot convert an object to type NULL");
        }

        @Override
        protected boolean isInternal() {
            return true;
        }
    },

    SHORT(Collections.singletonList(Short.class)) {
        @Override
        public Short convert(@Nullable final Object o) {
            return Optional.ofNullable(o).map(TypeConverter::getShort).orElse(null);
        }
    },

    INTEGER(Collections.singletonList(Integer.class)) {
        @Override
        public Integer convert(final Object o) {
            return Optional.ofNullable(o).map(TypeConverter::getInt).orElse(null);
        }
    },

    LONG(Collections.singletonList(Long.class)) {
        @Override
        public Long convert(@Nullable final Object o) {
            return Optional.ofNullable(o).map(TypeConverter::getLong).orElse(null);
        }
    },

    FLOAT(Collections.singletonList(Float.class)) {
        @Override
        public Float convert(@Nullable final Object o) {
            return Optional.ofNullable(o).map(TypeConverter::getFloat).orElse(null);
        }
    },

    DOUBLE(Collections.singletonList(Double.class)) {
        @Override
        public Double convert(@Nullable final Object o) {
            return Optional.ofNullable(o).map(TypeConverter::getDouble).orElse(null);
        }
    },

    BOOLEAN(Collections.singletonList(Boolean.class)) {
        @Override
        public Boolean convert(@Nullable final Object o) {
            return Optional.ofNullable(o).map(TypeConverter::getBool).orElse(null);
        }
    },

    STRING(Collections.singletonList(String.class)) {
        @Override
        public String convert(@Nullable final Object o) {
            return Optional.ofNullable(o).map(TypeConverter::getString).orElse(null);
        }
    },

    LIST(List.of(Collection.class, List.class, Set.class)) {
        @Override
        public Collection convert(@Nullable final Object o) {
            return Optional.ofNullable(o)
                    .map(it -> TypeConverter.getList(it, true))
                    .orElse(null);
        }

    },
    MAP(List.of(Map.class)) {
        @Override
        @SuppressWarnings("unchecked")
        public Map<String, Object> convert(@Nullable final Object o) {
            return Optional.ofNullable(o)
                    .map(it -> (Map<String, Object>) o)
                    .orElse(null);
        }

    },
    BYTES(Collections.emptyList()) {
        @Override
        public byte[] convert(@Nullable final Object o) {
            return Optional.ofNullable(o).map(TypeConverter::getBytes).orElse(null);
        }
    };

    private final static Map<Class<?>, Type> JAVA_CLASS_TYPES = new HashMap<>();

    static {
        for (Type type : Type.values()) {
            if (!type.isInternal()) {
                for (Class<?> typeClass : type.classes) {
                    JAVA_CLASS_TYPES.put(typeClass, type);
                }
            }
        }
    }

    private final Collection<Class<?>> classes;

    /**
     * Creates a new {@link Type} instance.
     */
    Type(final Collection<Class<?>> classes) {
        this.classes = classes;
    }

    /**
     * Converts the specified object to this type.
     *
     * @param o The object to be converted - can be null.
     * @return The converted object, or {@code null} if the passed object is {@code null}.
     */
    public abstract Object convert(@Nullable final Object o);

    /**
     * Checks whether this is type is internal.
     * Internal types cannot be resolved from a class or string name.
     *
     * @return {@code false}.
     */
    protected boolean isInternal() {
        return false;
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