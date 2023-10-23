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
import io.streamthoughts.jikkou.core.extension.builder.ExtensionDescriptorBuilder;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
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

        final ExtensionMetadata metadata = loadAnnotationComponentMetadata(extensionType);

        return new ExtensionDescriptorBuilder<T>()
                .type(extensionType)
                .name(Extension.getName(extensionType))
                .description(Extension.getDescription(extensionType))
                .category(Extension.getCategory(extensionType))
                .isEnabled(Extension.isEnabled(extensionType))
                .supplier(extensionSupplier)
                .classLoader(classLoader)
                .metadata(metadata)
                .build();
    }

    private <T> ExtensionMetadata loadAnnotationComponentMetadata(final Class<T> extensionType) {
        final ExtensionMetadata metadata = new ExtensionMetadata();
        var annotations = AnnotationResolver.findAllAnnotations(extensionType);
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            var aTribute = new ExtensionAttribute(aTributeNameFor(type));
            for (var method : type.getDeclaredMethods()) {
                try {
                    Object defaultValue = method.getDefaultValue();
                    Object value = method.invoke(annotation);
                    aTribute.add(method.getName(), value, defaultValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LOG.error("Error while scanning component annotations", e);
                }
            }
            metadata.addAttribute(aTribute);
        }
        return metadata;
    }

    private String aTributeNameFor(final Class<? extends Annotation> annotationType) {
        return annotationType.getSimpleName().toLowerCase();
    }
}
