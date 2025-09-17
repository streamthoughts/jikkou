/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.common.utils.Classes;
import io.streamthoughts.jikkou.core.extension.exceptions.ConflictingExtensionDefinitionException;
import io.streamthoughts.jikkou.core.extension.exceptions.ExtensionRegistrationException;
import io.streamthoughts.jikkou.core.extension.exceptions.NoUniqueExtensionException;
import io.streamthoughts.jikkou.core.extension.qualifier.Qualifiers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@link ExtensionDescriptorRegistry}.
 */
public final class DefaultExtensionRegistry implements ExtensionRegistry, ExtensionDescriptorRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultExtensionRegistry.class);

    private final Map<Class, List<ExtensionDescriptor<?>>> descriptorsByType;

    // Multiple classes with the same FQCN can be loaded using different ClassLoader.
    private final Map<String, List<Class>> extensionsTypesByAlias;

    private final ExtensionDescriptorFactory descriptorFactory;

    private final ExtensionAliasesGenerator extensionAliasesGenerator;

    private final Map<ExtensionKey<?>, ExtensionSupplier<?>> extensionsByKey;

    private final List<ExtensionDescriptor<?>> descriptors;

    /**
     * Creates a new {@link DefaultExtensionRegistry} instance.
     *
     * @param descriptorFactory         the ExtensionDescriptor factory.
     * @param extensionAliasesGenerator the ExtensionAliasesGenerator.
     */
    public DefaultExtensionRegistry(@NotNull ExtensionDescriptorFactory descriptorFactory,
                                    @Nullable ExtensionAliasesGenerator extensionAliasesGenerator) {
        this.descriptorFactory = Objects.requireNonNull(descriptorFactory, "descriptorFactory must not be null");
        this.descriptorsByType = new HashMap<>();
        this.extensionsTypesByAlias = new HashMap<>();
        this.extensionsByKey = new HashMap<>();
        this.extensionAliasesGenerator = extensionAliasesGenerator;
        this.descriptors = new ArrayList<>();
    }

    /**
     * Copy constructor.
     */
    private DefaultExtensionRegistry(DefaultExtensionRegistry registry) {
        this.descriptorFactory = registry.descriptorFactory;
        this.extensionAliasesGenerator = registry.extensionAliasesGenerator;
        this.extensionsByKey = new HashMap<>(registry.extensionsByKey);
        this.descriptorsByType = new HashMap<>();
        this.extensionsTypesByAlias = new HashMap<>();
        this.descriptors = new ArrayList<>(registry.descriptors);
        registry.descriptorsByType.forEach((k, v) -> this.descriptorsByType.put(k, new ArrayList<>(v)));
        registry.extensionsTypesByAlias.forEach((k, v) -> this.extensionsTypesByAlias.put(k, new ArrayList<>(v)));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ExtensionDescriptor<?>> getAllDescriptors() {
        return Collections.unmodifiableList(descriptors);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> List<ExtensionDescriptor<T>> findAllDescriptorsByClass(@NotNull Class<T> type) {
        return findAllDescriptorsByClass(type, null);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ExtensionDescriptor<?>> findAllDescriptors(@NotNull Qualifier qualifier) {
        Stream<ExtensionDescriptor<?>> candidates = descriptors.stream();
        return qualifier.filter(null, candidates).toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> List<ExtensionDescriptor<T>> findAllDescriptorsByClass(@NotNull Class<T> type,
                                                                      @Nullable Qualifier<T> qualifier) {
        Objects.requireNonNull(type, "Cannot find descriptors for type 'null'");
        Stream<ExtensionDescriptor<T>> candidates = findDescriptorCandidatesByType(type);
        if (qualifier != null) {
            candidates = qualifier.filter(type, candidates);
        }
        return candidates.toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> List<ExtensionDescriptor<T>> findAllDescriptorsByAlias(@NotNull String alias) {
        return findAllDescriptorsByAlias(alias, null);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> List<ExtensionDescriptor<T>> findAllDescriptorsByAlias(@NotNull String alias,
                                                                      @Nullable Qualifier<T> qualifier) {
        Objects.requireNonNull(alias, "Cannot find descriptors for alias 'null'");

        List<Class> types = extensionsTypesByAlias.get(alias);
        if (types == null)
            return Collections.emptyList();

        @SuppressWarnings("unchecked")
        List<ExtensionDescriptor<T>> descriptors = types
            .stream()
            .flatMap(type -> {
                Stream<ExtensionDescriptor<T>> candidates = findDescriptorCandidatesByType(type);
                return qualifier != null ? qualifier.filter(type, candidates) : candidates;
            })
            .toList();
        return descriptors;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> Optional<ExtensionDescriptor<T>> findDescriptorByAlias(@NotNull String alias) {
        return findDescriptorByAlias(alias, null);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> Optional<ExtensionDescriptor<T>> findDescriptorByAlias(@NotNull String alias,
                                                                      @Nullable Qualifier<T> qualifier) {
        return findUniqueDescriptor(alias, findAllDescriptorsByAlias(alias, qualifier).stream());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> Optional<ExtensionDescriptor<T>> findDescriptorByClass(@NotNull Class<T> type) {
        Stream<ExtensionDescriptor<T>> candidates = findDescriptorCandidatesByType(type);
        return findUniqueDescriptor(type.getName(), candidates);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> Optional<ExtensionDescriptor<T>> findDescriptorByClass(@NotNull Class<T> type,
                                                                      @Nullable Qualifier<T> qualifier) {
        Stream<ExtensionDescriptor<T>> candidates = findDescriptorCandidatesByType(type);
        if (qualifier != null) {
            candidates = qualifier.filter(type, candidates);
        }
        return findUniqueDescriptor(type.getName(), candidates);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @SuppressWarnings("unchecked")
    public <T> ExtensionSupplier<T> getExtensionSupplier(final ExtensionDescriptor<T> descriptor) {
        return (ExtensionSupplier<T>) extensionsByKey.get(ExtensionKey.create(descriptor));
    }

    private <T> Optional<ExtensionDescriptor<T>> findUniqueDescriptor(final String type,
                                                                      final Stream<ExtensionDescriptor<T>> candidates) {
        List<ExtensionDescriptor<T>> descriptors = candidates.toList();
        if (descriptors.size() <= 1)
            return descriptors.stream().findFirst();

        final int numMatchingComponents = descriptors.size();

        throw new NoUniqueExtensionException("Expected single matching extension for " +
            "type '" + type + "' but found " + numMatchingComponents);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> void register(@NotNull Class<T> type, @NotNull Supplier<T> supplier) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(supplier, "supplier must not be null");
        ExtensionDescriptor<T> descriptor = descriptorFactory.make(
            type,
            supplier
        );
        registerDescriptor(descriptor);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> void register(@NotNull Class<T> type,
                             @NotNull Supplier<T> supplier,
                             ExtensionDescriptorModifier... modifiers) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(supplier, "supplier must not be null");
        ExtensionDescriptor<T> descriptor = descriptorFactory.make(
            type,
            supplier
        );
        for (ExtensionDescriptorModifier modifier : modifiers) {
            descriptor = modifier.apply(descriptor);
        }
        registerDescriptor(descriptor);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> void registerDescriptor(@NotNull ExtensionDescriptor<T> descriptor) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        LOG.info("Registering extension descriptor for name='{}', type='{}'",
            descriptor.name(),
            descriptor.type()
        );

        if (descriptor.name() == null)
            throw new ExtensionRegistrationException("Cannot register extension with name 'null': " + descriptor);

        if (descriptor.type() == null)
            throw new ExtensionRegistrationException("Cannot register extension with type 'null': " + descriptor);

        final ExtensionKey<T> key = ExtensionKey.create(descriptor);

        if (extensionsByKey.put(key, new DefaultExtensionSupplier<>(descriptor)) != null) {
            throw new ConflictingExtensionDefinitionException(
                "Failed to register ExtensionDescriptor, extension already exists for key: " + key);
        }
        registerAliasesFor(descriptor);

        Classes.getAllSuperTypes(descriptor.type()).forEach(cls -> {
            descriptorsByType.computeIfAbsent(cls, k -> new LinkedList<>()).add(descriptor);
            if (!cls.equals(descriptor.type())) {
                extensionsTypesByAlias.computeIfAbsent(cls.getName(), k -> new LinkedList<>()).add(descriptor.type());
            }
        });
        descriptors.add(descriptor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionDescriptorRegistry duplicate() {
        return new DefaultExtensionRegistry(this);
    }

    private void registerAliasesFor(final ExtensionDescriptor<?> descriptor) {

        final List<String> aliases = new LinkedList<>();
        aliases.add(descriptor.className());

        if (extensionAliasesGenerator != null) {
            Set<String> computed = extensionAliasesGenerator.getAliasesFor(descriptor, descriptors());
            if (!aliases.isEmpty()) {
                LOG.info("Registered aliases '{}' for extension {}.", computed, descriptor.className());
                descriptor.addAliases(computed);
                aliases.addAll(computed);
            }
        }
        aliases.forEach(alias -> {
            List<Class> types = extensionsTypesByAlias.computeIfAbsent(alias, key -> new LinkedList<>());
            types.add(descriptor.type());
        });
    }

    @SuppressWarnings("unchecked")
    private <T> Stream<ExtensionDescriptor<T>> findDescriptorCandidatesByType(final Class<T> type) {
        return descriptorsByType.getOrDefault(type, Collections.emptyList())
            .stream()
            .map(d -> (ExtensionDescriptor<T>) d);
    }

    private List<ExtensionDescriptor<?>> descriptors() {
        return descriptorsByType.values().stream()
            .flatMap(Collection::stream)
            .toList();
    }


    private record ExtensionKey<T>(Class<T> type, Qualifier<T> qualifier) {

        static <T> ExtensionKey<T> create(final ExtensionDescriptor<T> descriptor) {
            return new ExtensionKey<>(descriptor.type(), Qualifiers.byName(descriptor.name()));
        }
    }
}
