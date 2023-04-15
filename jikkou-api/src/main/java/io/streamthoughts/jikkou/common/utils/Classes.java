/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.common.utils;

import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class to manipulate {@link Class} objects.
 */
public final class Classes {

    private Classes() {}

    public static <T> T newInstance(final Class<T> cls, final ClassLoader classLoader) {
        ClassLoader saveLoader = Classes.compareAndSwapLoaders(classLoader);
        try {
            return Classes.newInstance(cls);
        } finally {
            Classes.compareAndSwapLoaders(saveLoader);
        }
    }

    public static Class<?> forName(final String cls) {
        try {
            return Class.forName(cls);
        } catch (ClassNotFoundException e) {
            throw new JikkouRuntimeException("Failed to get class for name '" + cls + "'", e);
        }
    }

    public static <T> T newInstance(final Class<T> c) {
        if (c == null)
            throw new RuntimeException("class cannot be null");
        try {
            return c.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new JikkouRuntimeException("Could not find a public no-argument constructor for " + c.getName(), e);
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new JikkouRuntimeException("Could not instantiate class " + c.getName(), e);
        }
    }

    public static ClassLoader compareAndSwapLoaders(final ClassLoader classLoader) {
        final ClassLoader current = Thread.currentThread().getContextClassLoader();
        if (!current.equals(classLoader)) {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        return current;
    }

    public static ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null)
            return Classes.class.getClassLoader();
        else
            return cl;
    }

    public static boolean canBeInstantiated(final Class<?> cls) {
        Objects.requireNonNull(cls, "cls cannot be null");
        return !cls.isInterface() && !Modifier.isAbstract(cls.getModifiers());
    }


    public static Set<Class<?>> getAllSuperTypes(final Class<?> type) {
        Set<Class<?>> result = new LinkedHashSet<>();
        if (type != null && !type.equals(Object.class)) {
            result.add(type);
            for (Class<?> supertype : getSuperTypes(type)) {
                result.addAll(getAllSuperTypes(supertype));
            }
        }
        return result;
    }

    public static Set<Class<?>> getSuperTypes(final Class<?> type) {
        Set<Class<?>> result = new LinkedHashSet<>();
        Class<?> superclass = type.getSuperclass();
        Class<?>[] interfaces = type.getInterfaces();
        if (superclass != null && !superclass.equals(Object.class)) {
            result.add(superclass);
        }
        if (interfaces != null && interfaces.length > 0) {
            result.addAll(Arrays.asList(interfaces));
        }
        return result;
    }
}
