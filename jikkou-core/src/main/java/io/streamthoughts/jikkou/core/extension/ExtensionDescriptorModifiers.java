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

import io.streamthoughts.jikkou.core.extension.builder.ExtensionDescriptorBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Standard {@link ExtensionDescriptorModifier}.
 */
public final class ExtensionDescriptorModifiers {

    /**
     * Gets a modifier implementation that will set the provider of the extension.
     *
     * @return a new {@link ExtensionDescriptorModifier} instance.
     */
    public static ExtensionDescriptorModifier withProvider(@NotNull final String name) {
        return new ExtensionDescriptorModifier() {
            @Override
            public <T> ExtensionDescriptor<T> apply(final ExtensionDescriptor<T> descriptor) {
                return ExtensionDescriptorBuilder.<T>create(descriptor)
                        .provider(name)
                        .build();
            }
        };
    }

    /**
     * Gets a modifier implementation that will set the name of the extension.
     *
     * @return a new {@link ExtensionDescriptorModifier} instance.
     */
    public static ExtensionDescriptorModifier withName(@NotNull final String name) {
        return new ExtensionDescriptorModifier() {
            @Override
            public <T> ExtensionDescriptor<T> apply(final ExtensionDescriptor<T> descriptor) {
                return ExtensionDescriptorBuilder.<T>create(descriptor)
                        .name(name)
                        .build();
            }
        };
    }

    /**
     * Gets a modifier implementation that will enable the extension.
     *
     * @return a new {@link ExtensionDescriptorModifier} instance.
     */
    public static ExtensionDescriptorModifier enabled(final boolean enabled) {
        return new ExtensionDescriptorModifier() {
            @Override
            public <T> ExtensionDescriptor<T> apply(final ExtensionDescriptor<T> descriptor) {
                return ExtensionDescriptorBuilder.<T>create(descriptor)
                        .isEnabled(enabled)
                        .build();
            }
        };
    }
}
