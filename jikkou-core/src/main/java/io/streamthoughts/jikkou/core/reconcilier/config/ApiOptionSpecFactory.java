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
package io.streamthoughts.jikkou.core.reconcilier.config;

import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.models.ApiOptionSpec;
import io.streamthoughts.jikkou.core.models.ApiResourceVerbOptionList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class to create new {@link ApiResourceVerbOptionList}.
 */
public final class ApiOptionSpecFactory {

    /**
     * Creates a new {@link ApiResourceVerbOptionList} for the specified extension class.
     *
     * @param descriptor the extension descriptor.
     * @return a new {@link ApiResourceVerbOptionList} object.
     */
    public List<ApiOptionSpec> make(@NotNull ExtensionDescriptor<?> descriptor) {
        Objects.requireNonNull(descriptor, "descriptor cannot be null");
        return descriptor.properties()
                .stream()
                .map(config -> new ApiOptionSpec(
                        config.name(),
                        config.description(),
                        config.type(),
                        config.defaultValue(),
                        config.required())
                ).toList();
    }
}
