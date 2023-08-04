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
package io.streamthoughts.jikkou.api.selector.internal;

import io.streamthoughts.jikkou.api.error.SelectorException;
import java.lang.reflect.Method;
import java.util.Objects;

public class ReflectivePropertyAccessor implements PropertyAccessor {

    private static final String GETTER_PREFIX = "get";
    private static final String DOT = ".";
    private static final Object NO_VALUE = null;

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
    public Object read(final Object target,
                       final String name) throws SelectorException {
        Objects.requireNonNull(target, "target cannot be null");
        Objects.requireNonNull(name, "name cannot be null");

        Class<?> type = (target instanceof Class) ? (Class<?>) target : target.getClass();

        try {
            Method method = findGetterMethodForProperty(type, name);
            if (method != null || (method = findAccessMethodForProperty(type, name)) != null) {
                method.setAccessible(true);
                return method.invoke(target);
            }

            if (isDotPropertyAccessPath(name)) {
                String[] split = name.split("\\.", 2);
                Method rootMethod = findGetterMethodForProperty(type, split[0]);
                if (rootMethod != null || (rootMethod = findAccessMethodForProperty(type, name)) != null) {
                    rootMethod.setAccessible(true);
                    Object rootObject = rootMethod.invoke(target);
                    return new PropertyAccessors().readPropertyValue(rootObject, split[1]);
                }
            }
        } catch (Exception e) {
            throw new SelectorException(e.getMessage());
        }

        return NO_VALUE;
    }

    private Method findGetterMethodForProperty(final Class<?> target, final String name) {
        for (Method m : target.getMethods()) {
            String methodName = m.getName();
            if (methodName.equals(GETTER_PREFIX + getMethodSuffixForProperty(name))) {
                return m;
            }
        }
        return null;
    }

    private Method findAccessMethodForProperty(final Class<?> target, final String name) {
        for (Method m : target.getMethods()) {
            String methodName = m.getName();
            if (methodName.equals(name)) {
                return m;
            }
        }
        return null;
    }

    private String getMethodSuffixForProperty(final String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static boolean isDotPropertyAccessPath(final String name) {
        return name.contains(DOT);
    }
}
