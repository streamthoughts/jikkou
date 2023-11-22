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

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.exceptions.ExtensionCreationException;

/**
 *
 * @param <T> the type of the extension returned from this supplier.
 */
public interface ExtensionSupplier<T> {

    /**
     * Create a new extension instance.
     *
     * @param configuration the {@link Configuration} that can be used to configure the extension.
     * @return a new instance of {@link T}.
     *
     * @throws ExtensionCreationException if the extension cannot be created or configured.
     */
    T get(Configuration configuration);

    /**
     * Gets the descriptor for the extension supplied by this class.
     *
     * @return  the {@link ExtensionDescriptor}.
     */
    ExtensionDescriptor<T> descriptor();
}
