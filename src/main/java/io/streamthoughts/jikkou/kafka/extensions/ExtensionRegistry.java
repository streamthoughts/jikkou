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
package io.streamthoughts.jikkou.kafka.extensions;

import io.streamthoughts.jikkou.kafka.internal.ClassUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExtensionRegistry {

    private final Map<String, Supplier<? extends Extension>> extensions;

    /**
     * Creates a new {@link ExtensionRegistry} instance.
     */
    public ExtensionRegistry() {
        this.extensions = new HashMap<>();
    }

    public void register(final String extensionClass,
                         final Supplier<? extends Extension> supplier) {
        this.extensions.put(extensionClass, supplier);
    }

    public Collection<ExtensionDescription> allRegisteredExtensions() {
        return extensions.keySet()
                .stream()
                .map(ExtensionDescription::new)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public <T extends Extension> T getExtensionForClass(final String extensionClass) {
        if (extensions.containsKey(extensionClass)) {
            @SuppressWarnings("unchecked")
            final T extension = (T) extensions.get(extensionClass).get();
            return extension;
        }
        @SuppressWarnings("unchecked")
        final T result = getExtensionForClass((Class<T>) ClassUtils.forName(extensionClass));
        return result;
    }

    public <T extends Extension> T getExtensionForClass(final Class<T> extensionClass) {
        return ClassUtils.newInstance(extensionClass);
    }

}
