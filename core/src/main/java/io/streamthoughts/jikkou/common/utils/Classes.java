/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
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
