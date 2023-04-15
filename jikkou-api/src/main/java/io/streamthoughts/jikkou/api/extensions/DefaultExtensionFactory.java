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
import io.streamthoughts.jikkou.common.utils.Classes;
import io.streamthoughts.jikkou.common.utils.CollectionUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultExtensionFactory implements ExtensionFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultExtensionFactory.class);

    private final Map<Class<?>, List<Supplier<? extends Extension>>> suppliersByTypes;

    private final Map<String, List<Supplier<? extends Extension>>> suppliersByAliases;

    private final List<ExtensionDescriptor<?>> descriptors;

    private final Map<Class<?>, List<ExtensionDescriptor<?>>> descriptorsByTypes;

    /**
     * Creates a new {@link DefaultExtensionFactory} instance.
     */
    public DefaultExtensionFactory() {
        this.suppliersByTypes = new HashMap<>();
        this.suppliersByAliases = new HashMap<>();
        this.descriptorsByTypes = new HashMap<>();
        this.descriptors = new LinkedList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> void register(final @NotNull Class<T> type,
                                               final @NotNull Supplier<T> supplier) {
        LOG.info("Registered extension for type {}", type.getName());
        var descriptor = registerExtensionDescriptorForClassAndGet(type);

        descriptor.aliases().forEach(alias -> suppliersByAliases.computeIfAbsent(
                type.getSimpleName(),
                k -> new LinkedList<>()).add(supplier));

        Classes.getAllSuperTypes(type).forEach(cls -> {
            suppliersByTypes.computeIfAbsent(cls, k -> new LinkedList<>()).add(supplier);
            descriptorsByTypes.computeIfAbsent(cls, k -> new LinkedList<>()).add(descriptor);
            suppliersByAliases.computeIfAbsent(cls.getName(), k -> new LinkedList<>()).add(supplier);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> List<Supplier<T>> getAllExtensionSupplier(@NotNull final Class<T> type) {
        return this.<T>findAllExtensionsSuppliersByClass(type).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> List<Supplier<T>> getAllExtensionsSupplier(@NotNull final String type) {
        return this.<T>findAllExtensionsSuppliersByAlias(type).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> Supplier<T> getExtensionSupplier(@NotNull final Class<T> type) {
        Optional<Supplier<T>> optional = findExtensionSupplierByClass(type);
        return optional.orElseThrow(() -> new NoSuchExtensionException(
                "No extension registered for type '" + type + "'")
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> Supplier<T> getExtensionSupplier(@NotNull final String type) {
        Optional<Supplier<T>> optional = findExtensionSupplierByAlias(type);
        return optional.orElseThrow(() -> new NoSuchExtensionException(
                "No extension registered for type '" + type + "'")
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> List<T> getAllExtensions(final Class<T> type,
                                                          final Configuration config,
                                                          final Predicate<ExtensionDescriptor<T>> predicate) {
        return getAllDescriptorsForType(type)
                .stream()
                .filter(predicate)
                .map(it -> getExtensionSupplier(it.clazz()))
                .map(Supplier::get)
                .map(extension -> configureAndGet(config, extension))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> T getExtension(final String type,
                                                final Configuration config) {

        Optional<Supplier<T>> optional = this.findExtensionSupplierByAlias(type);

        T extension = optional
                .map(Supplier::get)
                .or(() -> newExtensionInstanceForClass(type))
                .orElseThrow(() -> new NoSuchExtensionException(
                        "No extension registered for type '" + type + "'")
                );

        return configureAndGet(config, extension);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> T getExtension(final Class<T> type,
                                                final Configuration config) {

        Optional<Supplier<T>> optional = this.findExtensionSupplierByClass(type);

        T extension = optional
                .map(Supplier::get)
                .or(() -> newExtensionInstanceForClass(type))
                .orElseThrow(() -> new NoSuchExtensionException(
                        "No extension registered for type '" + type + "'")
                );

        return configureAndGet(config, extension);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ExtensionDescriptor<?>> allExtensionTypes() {
        return new TreeSet<>(descriptors);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Extension> Collection<ExtensionDescriptor<T>> getAllDescriptorsForType(@NotNull final Class<T> type) {
        List<ExtensionDescriptor<?>> descriptors = descriptorsByTypes.get(type);
        return new TreeSet<>(CollectionUtils.cast(descriptors));
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private <T extends Extension> Stream<Supplier<T>> findAllExtensionsSuppliersByClass(
            @NotNull final Class<T> extensionClass) {
        return Optional
                .ofNullable(suppliersByTypes.get(extensionClass))
                .stream()
                .flatMap(Collection::stream)
                .map(it -> (Supplier<T>) it);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private <T extends Extension> Stream<Supplier<T>> findAllExtensionsSuppliersByAlias(
            @NotNull final String extensionAlias) {
        return Optional
                .ofNullable(suppliersByAliases.get(extensionAlias))
                .stream()
                .flatMap(Collection::stream)
                .map(it -> (Supplier<T>) it);
    }

    private <T extends Extension> Optional<Supplier<T>> findExtensionSupplierByClass(@NotNull final Class<T> type) {
        List<Supplier<T>> suppliers = this.findAllExtensionsSuppliersByClass(type).toList();
        if (suppliers.isEmpty()) return Optional.empty();

        int numMatchingExtensions = suppliers.size();
        if (numMatchingExtensions == 1) {
            Supplier<T> supplier = suppliers.get(0);
            return Optional.of(supplier);
        }

        throw new NoUniqueExtensionException("Expected single matching extension for " +
                "type '" + type.getName() + "' but found " + numMatchingExtensions);
    }

    private <T extends Extension> Optional<Supplier<T>> findExtensionSupplierByAlias(@NotNull final String type) {
        List<Supplier<T>> suppliers = this.<T>findAllExtensionsSuppliersByAlias(type).toList();
        if (suppliers.isEmpty()) return Optional.empty();

        int numMatchingExtensions = suppliers.size();
        if (numMatchingExtensions == 1) {
            Supplier<T> supplier = suppliers.get(0);
            return Optional.of(supplier);
        }

        throw new NoUniqueExtensionException("Expected single matching extension for " +
                "type '" + type + "' but found " + numMatchingExtensions);

    }

    private ExtensionDescriptor<?> registerExtensionDescriptorForClassAndGet(final Class<? extends Extension> type) {

        List<String> aliases = new LinkedList<>();
        aliases.add(type.getSimpleName());

        var descriptor = new ExtensionDescriptor<>(
                type,
                type.getName(),
                aliases
        );
        descriptors.add(descriptor);
        return descriptor;
    }

    private static <T extends Extension> T configureAndGet(final Configuration config, final T extension) {
        extension.configure(config);
        return extension;
    }

    private static <T> Optional<T> newExtensionInstanceForClass(final Class<T> type) {
        return Optional.of(Classes.newInstance(type));
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<T> newExtensionInstanceForClass(final String type) {
        try {
            Class<T> extensionClass = (Class<T>) Class.forName(type);
            return Optional.of(Classes.newInstance(extensionClass));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
