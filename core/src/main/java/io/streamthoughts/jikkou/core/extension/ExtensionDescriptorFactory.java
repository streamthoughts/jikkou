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

import io.streamthoughts.jikkou.core.extension.exceptions.ExtensionRegistrationException;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * Factory to create new {@link ExtensionDescriptorFactory} instance.
 */
public interface ExtensionDescriptorFactory {

    /**
     * Makes a new {@link ExtensionDescriptorFactory} instance.
     *
     * @param extensionType     the type of the extension. Cannot be {@code null}.
     * @param extensionSupplier the supplier of the extension. Cannot be {@code null}.
     * @return a new instance of {@link ExtensionDescriptor}.
     * @throws ExtensionRegistrationException if an exception occurred while building the descriptor.
     */
    <T> ExtensionDescriptor<T> make(@NotNull final Class<T> extensionType,
                                    @NotNull final Supplier<T> extensionSupplier
    );
}