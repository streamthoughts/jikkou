/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.config.internals.Type;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.extension.exceptions.NoSuchExtensionException;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default ExtensionContext.
 */
public final class DefaultExtensionContext implements ExtensionContext {

    private final ExtensionFactory factory;
    private final ExtensionDescriptor<?> descriptor;
    private final Map<String, ConfigProperty> propertiesByName;

    /**
     * Creates a new {@link DefaultExtensionContext} instance.
     *
     * @param descriptor    The ExtensionDescriptor
     */
    public DefaultExtensionContext(final ExtensionFactory factory,
                                   final ExtensionDescriptor<?> descriptor) {
        this.factory = factory;
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor cannot be null");
        this.propertiesByName = getConfigProperties(descriptor)
            .stream()
            .collect(Collectors.toMap(ConfigProperty::key, Function.identity()));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String name() {
        return descriptor.name();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Configuration configuration() {
        return descriptor.configuration();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Map<String, ConfigProperty> configProperties() {
        return Collections.unmodifiableMap(propertiesByName);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> ConfigProperty<T> configProperty(final String key) {
        if (Strings.isBlank(key)) throw new IllegalArgumentException("key cannot be null or empty");
        ConfigProperty<T> property = getConfigProperty(key);
        if (property == null) {
            throw new NoSuchElementException("Unknown config property for key '" + key + "'");
        }
        return property;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ExtensionContext contextForExtension(Class<? extends Extension> extension) {
        if (factory == null) throw new IllegalStateException("No factory configured");
        return factory.findDescriptorByClass(extension)
            .map(descriptor ->
                new DefaultExtensionContext(factory, descriptor)
            )
            .orElseThrow(() -> new NoSuchExtensionException("No extension registered for type: " + extension.getName()));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ExtensionProvider> T provider() {
        return (T) descriptor.providerSupplier().get();
    }

    @SuppressWarnings("unchecked")
    private <T> ConfigProperty<T> getConfigProperty(String key) {
        return (ConfigProperty<T>) propertiesByName.get(key);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static List<ConfigProperty> getConfigProperties(final ExtensionDescriptor<?> descriptor) {
        if (descriptor == null || descriptor.properties() == null) {
            return Collections.emptyList();
        }
        return descriptor.properties()
            .stream()
            .map(spec -> {
                Class specType = spec.type();
                Type type = Type.forClass(specType);
                ConfigProperty property = null;
                if (type != null) {
                    property = ConfigProperty.of(type, spec.name());
                } else if (Enum.class.isAssignableFrom(specType)) {
                    Class<Enum> enumType = (Class<Enum>) specType;
                    property = ConfigProperty.ofEnum(spec.name(), enumType);
                } else {
                    property = ConfigProperty.of(spec.name(), TypeConverter.of(specType));
                }
                property = property.description(spec.description());
                if (!spec.required()) {
                    property = property.orElse(() -> type.converter().convertValue(spec.defaultValue()));
                }
                return property;
            })
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return String.format("ExtensionContext[name=%s, type=%s]", name(), descriptor.className());
    }
}
