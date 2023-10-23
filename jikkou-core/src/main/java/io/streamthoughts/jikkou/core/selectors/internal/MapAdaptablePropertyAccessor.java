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
package io.streamthoughts.jikkou.core.selectors.internal;

import io.streamthoughts.jikkou.core.exceptions.SelectorException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class MapAdaptablePropertyAccessor implements PropertyAccessor {

    private static final String GET_METHOD_NAME = "get";
    private static final String DOT = ".";
    private static final Object NO_VALUE = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return new Class[]{Map.class};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRead(final Object target,
                           final String name) throws SelectorException {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object read(final Object target,
                       final String name) throws SelectorException {
        Objects.requireNonNull(target, "target cannot be null");
        Objects.requireNonNull(name, "name cannot be null");

        Class<?> type = target instanceof Class ? (Class<?>) target : target.getClass();

        return Map.class.isAssignableFrom(type) ?
                readFromMapObject((Map) target, name) :
                readFromMapAdaptableObject(target, name, type);
    }

    private Object readFromMapAdaptableObject(final Object target,
                                              final String key,
                                              final Class<?> type) {
        try {
            Method method = findGetterByKeyMethodForProperty(type);
            if (method != null && method.canAccess(target)) {
                final Object result = method.invoke(target, key);

                if (result != null) return result;

                // If result is NULL, then we need to check whether the given key represents a dotted path.
                if (isDotPropertyAccessPath(key)) {
                    String[] split = key.split("\\.", 2);
                    Object rootObject = method.invoke(target, split[0]);
                    if (rootObject != null) {
                        return new PropertyAccessors().readPropertyValue(rootObject, split[1]);
                    }
                }
            }
            return NO_VALUE;

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new SelectorException(e.getMessage());
        }
    }

    private Object readFromMapObject(final Map<String, Object> target,
                                     final String key) {

        if (target.containsKey(key)) return target.get(key);

        // If key does NOT exist, then we need to check whether the given key uses dotted-notation.
        if (isDotPropertyAccessPath(key)) {
            final String[] split = key.split("\\.", 2);
            final String rootKey = split[0];
            if (target.containsKey(rootKey)) {
                Object rootObject = target.get(rootKey);
                if (rootObject != null) {
                    return new PropertyAccessors().readPropertyValue(rootObject, split[1]);
                }
            }
        }
        return NO_VALUE;
    }

    private Method findGetterByKeyMethodForProperty(final Class<?> target) {
        return findMethodForProperty(target, this::isAccessibleByKey);
    }

    private Method findMethodForProperty(final Class<?> target, final Predicate<Method> predicate) {
        Optional<Method> optional = Arrays.stream(target.getMethods())
                .filter(predicate)
                .findAny();
        return optional.orElse(null);
    }

    private boolean isAccessibleByKey(final Method m) {
        String methodName = m.getName();
        if (methodName.equals(GET_METHOD_NAME) && m.getParameterCount() == 1) {
            Class<?>[] parameterTypes = m.getParameterTypes();
            return parameterTypes[0].isAssignableFrom(String.class);
        }
        return false;
    }

    private static boolean isDotPropertyAccessPath(final String name) {
        return name.contains(DOT);
    }
}
