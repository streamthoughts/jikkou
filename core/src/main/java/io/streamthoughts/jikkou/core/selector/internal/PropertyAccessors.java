/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector.internal;

import io.streamthoughts.jikkou.core.exceptions.SelectorException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PropertyAccessors {

    private final List<PropertyAccessor> accessors;

    public PropertyAccessors() {
        accessors = List.of(
                new ConfigsPropertyAccessor(),
                new MapAdaptablePropertyAccessor(),
                new ReflectivePropertyAccessor()
        );
    }

    public Object readPropertyValue(final Object target,
                                    final String name) throws SelectorException {

        List<PropertyAccessor> specificAccessors = findSpecificAccessorsToRead(target, name);
        if (!specificAccessors.isEmpty()) {
            Object value = evaluateReaders(target, name, specificAccessors);
            if (value != null) return value;
        }

        List<PropertyAccessor> genericAccessors = findGenericAccessorsToRead(target, name);
        if (!genericAccessors.isEmpty()) {
            return evaluateReaders(target, name, genericAccessors);
        }

        throw new SelectorException(
                String.format(
                        "Cannot found any property accessor for type '%s' and property %s",
                        target.getClass().getCanonicalName(),
                        name
                )
        );
    }


    private Object evaluateReaders(final Object target,
                                   final String name,
                                   final List<PropertyAccessor> specifics) {
        Iterator<PropertyAccessor> it = specifics.iterator();
        Object value = null;
        while (it.hasNext() && value == null) {
            PropertyAccessor accessor = it.next();
            value = accessor.read(target, name);
        }
        return value;
    }


    /**
     * Helpers methods to find generic read accessors for the given arguments.
     *
     * @param target the target object.
     * @param name   the field name.
     * @return a list of {@link PropertyAccessor} candidates.
     */
    public List<PropertyAccessor> findGenericAccessorsToRead(final Object target,
                                                             final String name) {
        return accessors
                .stream()
                .filter(accessor -> !isSpecificAccessor(accessor))
                .filter(accessor -> accessor.canRead(target, name))
                .collect(Collectors.toList());
    }

    /**
     * Helpers methods to find specific read accessors for the given arguments.
     *
     * @param target the target object.
     * @param name   the field name.
     * @return a list of {@link PropertyAccessor} candidates.
     */
    public List<PropertyAccessor> findSpecificAccessorsToRead(final Object target,
                                                              final String name) {
        Class<?> type = target instanceof Class ? (Class<?>) target : target.getClass();
        return accessors
                .stream()
                .filter(accessor -> isAccessorSpecificForType(type, accessor))
                .filter(accessor -> accessor.canRead(target, name))
                .collect(Collectors.toList());
    }

    private static boolean isAccessorSpecificForType(final Class<?> type, PropertyAccessor accessor) {
        if (isSpecificAccessor(accessor)) {
            Class<?>[] specificTargetClasses = accessor.getSpecificTargetClasses();
            List<Class<?>> l = Arrays
                    .stream(specificTargetClasses)
                    .filter(clazz -> clazz.isAssignableFrom(type))
                    .toList();
            return l.size() > 0;
        }
        return false;
    }

    private static boolean isSpecificAccessor(final PropertyAccessor accessor) {
        return accessor.getSpecificTargetClasses() != null && accessor.getSpecificTargetClasses().length > 0;
    }
}