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

import org.jetbrains.annotations.NotNull;

/**
 * An abstract implementation of an {@link Extension} that manages the {@link ExtensionContext} instance.
 */
public abstract class ContextualExtension implements Extension {

    private ExtensionContext context;

    /** {@inheritDoc} **/
    @Override
    public void init(@NotNull ExtensionContext context) {
        this.context = context;
    }

    /**
     * Get the extension's context set during initialization.
     *
     * @return  The ExtensionContext.
     */
    public ExtensionContext extensionContext() {
        return context;
    }
}
