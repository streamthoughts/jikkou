/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.api.extensions;

import io.streamthoughts.jikkou.api.config.Configuration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class ReflectiveExtensionFactory extends DefaultExtensionFactory {

    private static final String ROOT_API_PACKAGE = "io.streamthoughts.jikkou";

    private final List<String> extensionPaths;
    private final List<String> extensionPackages;

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Creates a new {@link ReflectiveExtensionFactory} instance.
     */
    public ReflectiveExtensionFactory() {
        this.extensionPaths = new LinkedList<>();
        this.extensionPackages = new LinkedList<>();
    }

    public ReflectiveExtensionFactory addRootApiPackage() {
        return addExtensionPackage(ROOT_API_PACKAGE);
    }

    public ReflectiveExtensionFactory addExtensionPaths(final List<String> extensionPaths) {
        this.extensionPaths.addAll(extensionPaths);
        return this;
    }

    public ReflectiveExtensionFactory addExtensionPackage(final String extensionPackage) {
        this.extensionPackages.add(extensionPackage);
        return this;
    }

    public ReflectiveExtensionFactory addExtensionPackages(final List<String> extensionPackages) {
        this.extensionPackages.addAll(extensionPackages);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> T getExtension(@NotNull final String type,
                                                @NotNull final Configuration config) {
        mayScanForExtensions();
        return super.getExtension(type, config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> Collection<T> getAllExtensions(@NotNull final Class<T> type,
                                                                @NotNull final Configuration config) {
        mayScanForExtensions();
        return super.getAllExtensions(type, config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ExtensionDescriptor<?>> allExtensionTypes() {
        mayScanForExtensions();
        return super.allExtensionTypes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> Collection<ExtensionDescriptor<T>> allExtensionsDescriptorForType(@NotNull final Class<T> type) {
        mayScanForExtensions();
        return super.allExtensionsDescriptorForType(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> List<Supplier<T>> getAllExtensionSupplier(@NotNull final Class<T> type) {
        mayScanForExtensions();
        return super.getAllExtensionSupplier(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> List<Supplier<T>> getAllExtensionsSupplier(@NotNull final String type) {
        mayScanForExtensions();
        return super.getAllExtensionsSupplier(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> Supplier<T> getExtensionSupplier(@NotNull final Class<T> type) {
        mayScanForExtensions();
        return super.getExtensionSupplier(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> Supplier<T> getExtensionSupplier(@NotNull final String type) {
        mayScanForExtensions();
        return super.getExtensionSupplier(type);
    }


    private void mayScanForExtensions() {
        if (initialized.compareAndSet(false, true)) {
            ReflectiveExtensionScanner scanner = new ReflectiveExtensionScanner(this);
            if (!extensionPaths.isEmpty()) {
                scanner.scan(extensionPaths);
            }
            extensionPackages.forEach(scanner::scanForPackage);
        }
    }
}
