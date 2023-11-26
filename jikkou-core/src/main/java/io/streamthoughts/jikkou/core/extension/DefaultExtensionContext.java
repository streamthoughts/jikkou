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
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.config.internals.Type;
import io.streamthoughts.jikkou.core.extension.exceptions.NoSuchExtensionException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Default ExtensionContext.
 */
public final class DefaultExtensionContext implements ExtensionContext {

    private final Configuration appConfiguration;

    private final ExtensionDescriptorRegistry registry;
    private final ExtensionDescriptor<?> descriptor;
    private final Map<String, ConfigProperty> propertiesByName;

    /**
     * Creates a new {@link DefaultExtensionContext} instance.
     *
     * @param appConfiguration The Configuration.
     * @param descriptor       The ExtensionDescriptor
     */
    DefaultExtensionContext(@NotNull ExtensionDescriptorRegistry registry,
                            @NotNull ExtensionDescriptor<?> descriptor,
                            @NotNull Configuration appConfiguration) {
        this.registry = registry;
        this.appConfiguration = appConfiguration;
        this.descriptor = descriptor;
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
    public Configuration appConfiguration() {
        return appConfiguration;
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
        return registry.findDescriptorByClass(extension)
                .map(descriptor ->
                    new DefaultExtensionContext(registry, descriptor, appConfiguration)
                )
                .orElseThrow(() -> new NoSuchExtensionException("No extension registered for type: " + extension.getName()));
    }

    @SuppressWarnings("unchecked")
    private <T> ConfigProperty<T> getConfigProperty(String key) {
        return (ConfigProperty<T>) propertiesByName.get(key);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static List<ConfigProperty> getConfigProperties(final @NotNull ExtensionDescriptor<?> descriptor) {
        return descriptor.properties()
                .stream()
                .map(spec -> {
                    Type type = Type.forClass(spec.type());
                    ConfigProperty property = ConfigProperty
                            .of(type, spec.name())
                            .description(spec.description());
                    if (!spec.required()) {
                        property = property.orElse(() -> type.convert(spec.defaultValue()));
                    }
                    return property;
                })
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return String.format("ExtensionContext[name=%s, type=%s]", name(), descriptor.className());
    }
}
