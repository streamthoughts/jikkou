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

import java.util.List;
import java.util.Set;

/**
 * Default interface to generate aliases for an extension.
 */
public interface ExtensionAliasesGenerator {

    /**
     * Gets unique aliases for the specified {@link ExtensionDescriptor} descriptor.
     *
     * @param descriptor        the {@link ExtensionDescriptor} descriptor.
     * @param allDescriptors    the list of existing {@link ExtensionDescriptor} instances.
     *
     * @return                  the set of unique aliases.
     */
    Set<String> getAliasesFor(final ExtensionDescriptor<?> descriptor,
                              final List<ExtensionDescriptor<?>> allDescriptors);
}
