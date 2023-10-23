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
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Names;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import org.jetbrains.annotations.NotNull;

public class ResourceDescriptorFactory {

    /**
     * Makes a new {@link ResourceDescriptor} instance.
     *
     * @param type     the type of the resource. Cannot be {@code null}.
     * @param resource the class of the resource. Cannot be {@code null}.
     * @return a new instance of {@link ResourceDescriptor}.
     *
     * @throws NullPointerException if either type or resource is {@code  null}.
     */
    public ResourceDescriptor make(@NotNull final ResourceType type,
                                   @NotNull Class<? extends HasMetadata> resource) {
        Objects.requireNonNull(type, "Cannot make ResourceDescriptor for type 'null'");
        Objects.requireNonNull(type, "Cannot make ResourceDescriptor for resource 'null'");
        String description = extractDescription(resource);

        Names names = resource.getAnnotation(Names.class);
        if (names != null) {
            return new ResourceDescriptor(
                    type,
                    description,
                    resource,
                    Strings.isBlank(names.singular()) ? null : names.singular(),
                    Strings.isBlank(names.plural()) ? null : names.plural(),
                    new TreeSet<>(Arrays.asList(names.shortNames()))
            );
        } else {
            return new ResourceDescriptor(
                    type,
                    description,
                    resource,
                   null,
                    null,
                    Collections.emptySet()
            );
        }
    }

    @NotNull
    private static String extractDescription(@NotNull Class<? extends HasMetadata> resource) {
        return Optional.ofNullable(resource.getAnnotation(Description.class))
                .map(Description::value)
                .orElse("");
    }
}
