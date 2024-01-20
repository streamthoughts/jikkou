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

import io.streamthoughts.jikkou.common.annotation.AnnotationResolver;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.ConfigPropertySpec;
import io.streamthoughts.jikkou.core.extension.annotations.Category;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionSpec;
import io.streamthoughts.jikkou.core.extension.builder.ExtensionDescriptorBuilder;
import io.streamthoughts.jikkou.core.models.HasName;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default factory class to create new {@link ExtensionDescriptor} instance.
 */
public final class DefaultExtensionDescriptorFactory implements ExtensionDescriptorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultExtensionDescriptorFactory.class);
    private static final String PLACEHOLDER_COMPLETION_CANDIDATES = "${COMPLETION-CANDIDATES}";
    private static final String PLACEHOLDER_DEFAULT_VALUE = "${DEFAULT-VALUE}";

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
                .properties(getConfigProperties(extensionType))
                .isEnabled(isEnabled(extensionType))
                .supplier(extensionSupplier)
                .classLoader(classLoader)
                .metadata(metadata)
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
    static List<ConfigPropertySpec> getConfigProperties(@NotNull final Class<?> extensionType) {
        List<ExtensionSpec> annotations = AnnotationResolver.findAllAnnotationsByType(extensionType, ExtensionSpec.class);
        if (annotations == null || annotations.isEmpty()) {
            return Collections.emptyList();
        }

        return annotations
                .stream()
                .flatMap(annotation ->
                        Arrays.stream(annotation.options())
                                .map(spec -> {
                                    String defaultValue = null;
                                    String defaultValueSpec = spec.defaultValue();
                                    if (!ConfigPropertySpec.NO_DEFAULT_VALUE.equals(defaultValueSpec) &&
                                            !ConfigPropertySpec.NULL_VALUE.equals(defaultValueSpec)) {
                                        defaultValue = defaultValueSpec;
                                    }
                                    String description = null;
                                    String descriptionSpec = spec.description();
                                    if (!ConfigPropertySpec.NO_DEFAULT_VALUE.equals(descriptionSpec) &&
                                            !ConfigPropertySpec.NULL_VALUE.equals(descriptionSpec)) {
                                        description = descriptionSpec;

                                        if (description.contains(PLACEHOLDER_COMPLETION_CANDIDATES)) {
                                            if (Enum.class.isAssignableFrom(spec.type())) {
                                                String candidatesString = Arrays.stream(((Class<Enum>) spec.type()).getEnumConstants())
                                                        .map(Enum::name)
                                                        .collect(Collectors.joining(", "));
                                                description = description.replace(
                                                        PLACEHOLDER_COMPLETION_CANDIDATES, candidatesString);
                                            }
                                        }
                                        if (description.contains(PLACEHOLDER_DEFAULT_VALUE)) {
                                            description = description.replace(
                                                    PLACEHOLDER_DEFAULT_VALUE, spec.defaultValue());
                                        }
                                    }

                                    return new ConfigPropertySpec(
                                            spec.name(),
                                            spec.type(),
                                            description,
                                            defaultValue,
                                            spec.required()
                                    );
                                })).collect(Collectors.toList());
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
