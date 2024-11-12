/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.common.annotation.AnnotationResolver;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.ConfigPropertySpec;
import io.streamthoughts.jikkou.core.extension.annotations.Category;
import io.streamthoughts.jikkou.core.extension.builder.ExtensionDescriptorBuilder;
import io.streamthoughts.jikkou.core.models.HasConfig;
import io.streamthoughts.jikkou.core.models.HasName;
import io.streamthoughts.jikkou.core.models.HasPriority;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default factory class to create new {@link ExtensionDescriptor} instance.
 */
public final class DefaultExtensionDescriptorFactory implements ExtensionDescriptorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultExtensionDescriptorFactory.class);

    /**
     * Creates a new {@link DefaultExtensionDescriptorFactory} instance.
     */
    public DefaultExtensionDescriptorFactory() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> ExtensionDescriptor<T> make(@NotNull final Class<T> extensionType,
                                           @NotNull final Supplier<T> extensionSupplier) {

        final var classLoader = extensionType.getClassLoader();

        final ExtensionMetadata metadata = getExtensionMetadata(extensionType);

        return new ExtensionDescriptorBuilder<T>()
            .type(extensionType)
            .name(HasName.getName(extensionType))
            .title(getTitle(extensionType))
            .description(getDescription(extensionType))
            .examples(getExamples(extensionType))
            .category(getCategory(extensionType))
            .properties(getConfigProperties(extensionType, extensionSupplier))
            .isEnabled(isEnabled(extensionType))
            .supplier(extensionSupplier)
            .classLoader(classLoader)
            .metadata(metadata)
            .priority(getPriority(extensionType))
            .build();
    }

    /**
     * Gets the title of the specified extension class.
     *
     * @param extensionType The extension class.
     * @return The description or {@code null}.
     */
    @Nullable
    static <T> String getTitle(@NotNull final Class<T> extensionType) {
        return Optional.ofNullable(extensionType.getAnnotation(Title.class))
            .map(Title::value)
            .orElse(null);
    }

    /**
     * Gets the description of the specified extension class.
     *
     * @param extensionType The extension class.
     * @return The description or {@code null}.
     */
    static String getDescription(@NotNull final Class<?> extensionType) {
        return Optional
            .ofNullable(extensionType.getAnnotation(Description.class))
            .map(Description::value).orElse(null);
    }

    /**
     * Check whether the given extension is enabled.
     *
     * @param extensionType The extension class.
     * @return boolean, default is {@code true}.
     */
    static boolean isEnabled(@NotNull final Class<?> extensionType) {
        return AnnotationResolver.isAnnotatedWith(extensionType, Enabled.class);
    }

    /**
     * Gets the category of the specified extension class.
     *
     * @param extensionType The extension class.
     * @return the type or {@link ExtensionCategory#EXTENSION}.
     */
    @NotNull
    static ExtensionCategory getCategory(@NotNull final Class<?> extensionType) {
        return AnnotationResolver.findAllAnnotationsByType(extensionType, Category.class)
            .stream()
            .map(Category::value)
            .filter(Predicate.not(Predicate.isEqual(ExtensionCategory.EXTENSION)))
            .findFirst()
            .orElse(ExtensionCategory.EXTENSION);
    }

    /**
     * Gets the configuration property specifications of the specified extension class.
     *
     * @param extensionType The extension class.
     * @return The list of {@link ConfigPropertySpec}.
     */
    @NotNull
    static List<ConfigPropertySpec> getConfigProperties(@NotNull final Class<?> extensionType,
                                                        @NotNull final Supplier<?> extensionSupplier) {

        if (!HasConfig.class.isAssignableFrom(extensionType)) {
            return List.of();
        }

        HasConfig hasConfig = (HasConfig) extensionSupplier.get();
        return hasConfig.configProperties()
            .stream()
            .map(prop ->
                new ConfigPropertySpec(
                    prop.key(),
                    prop.rawType(),
                    prop.description(),
                    prop.defaultValue(),
                    prop.required()
                )
            )
            .toList();
    }

    /**
     * Gets the priority of the specified extension class.
     *
     * @param extensionType The extension class.
     * @return The priority
     */
    @NotNull
    static <T> Integer getPriority(@NotNull final Class<T> extensionType) {
        return Optional
            .ofNullable(extensionType.getAnnotation(Priority.class))
            .map(Priority::value).orElse(HasPriority.NO_ORDER);
    }

    /**
     * Gets the examples of the specified extension class.
     *
     * @param extensionType The extension class.
     * @return The list of {@link Example}.
     */
    @NotNull
    static <T> List<Example> getExamples(@NotNull final Class<T> extensionType) {
        return Arrays.stream(extensionType.getAnnotationsByType(io.streamthoughts.jikkou.core.annotation.Example.class))
            .map(ex -> new Example(ex.title(), ex.code()))
            .toList();
    }

    /**
     * Gets the metadata of the specified extension class.
     *
     * @param extensionType The extension class.
     * @return The {@link ExtensionMetadata}.
     */
    @NotNull
    static <T> ExtensionMetadata getExtensionMetadata(@NotNull final Class<T> extensionType) {
        final ExtensionMetadata metadata = new ExtensionMetadata();
        var annotations = AnnotationResolver.findAllAnnotations(extensionType);
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            var attribute = new ExtensionAttribute(attributeNameFor(type));
            for (var method : type.getDeclaredMethods()) {
                try {
                    Object defaultValue = method.getDefaultValue();
                    Object value = method.invoke(annotation);
                    attribute.add(method.getName(), value, defaultValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LOG.error("Error while scanning component annotations", e);
                }
            }
            metadata.addAttribute(attribute);
        }
        return metadata;
    }

    static String attributeNameFor(final Class<? extends Annotation> annotationType) {
        return annotationType.getSimpleName().toLowerCase();
    }
}
