/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public class ServiceLoaders {

    /**
     * Loads all services for the given type using the standard Java {@link java.util.ServiceLoader} mechanism.
     *
     * @param type the service Class type.
     * @param <T>  the service type.
     * @return the services implementing the given type.
     */
    public static <T> List<T> loadAllServices(final Class<T> type, final Set<ClassLoader> classLoaders) {
        final List<T> loaded = new LinkedList<>();
        final Set<Class<? extends T>> types = new HashSet<>();
        for (ClassLoader cl : classLoaders) {
            ServiceLoader<T> serviceLoader = ServiceLoader.load(type, cl);
            var providers = serviceLoader.stream().toList();
            for (ServiceLoader.Provider<T> provider : providers) {
                if (!types.contains(provider.type())) {
                    types.add(provider.type());
                    loaded.add(provider.get());
                }
            }
        }
        return loaded;
    }
}
