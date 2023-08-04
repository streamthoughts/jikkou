/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.api.extensions;

import io.streamthoughts.jikkou.api.model.HasMetadataAcceptable;
import io.streamthoughts.jikkou.api.model.ResourceType;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public record ExtensionDescriptor<T extends Extension>(
        @NotNull Class<T> clazz,
        @NotNull String classType,
        @NotNull List<String> aliases
        ) implements Comparable<ExtensionDescriptor<T>> {

    public String name() {
        return Extension.getName(clazz);
    }

    public String type() {
        return Extension.getType(clazz);
    }

    public String description() {
        return Extension.getDescription(clazz);
    }

    public boolean isEnabled() {
        return Extension.isEnabled(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@NotNull ExtensionDescriptor<T> that) {
        return that.classType().compareTo(this.classType());
    }

    public String getSource() {
        ClassLoader cl = getClass().getClassLoader();
        if (cl instanceof ExtensionClassLoader o) {
            return o.location();
        }
        return "<internal>";
    }

    public String getAliases() {
        return String.join(", ", aliases());
    }

    public List<ResourceType> getSupportedResources() {
        return HasMetadataAcceptable.getAcceptedResources(clazz)
                .stream()
                .toList();
    }

    public String getPrintableSupportedResources() {
        return  getSupportedResources()
                .stream()
                .map(ResourceType::getKind)
                .collect(Collectors.joining(", "));
    }
}
