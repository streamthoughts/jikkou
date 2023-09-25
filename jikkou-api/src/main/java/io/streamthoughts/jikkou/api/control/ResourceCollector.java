/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.api.control;

import io.streamthoughts.jikkou.annotation.AcceptsConfigProperty;
import io.streamthoughts.jikkou.annotation.ExtensionType;
import io.streamthoughts.jikkou.api.config.ConfigPropertyDescriptor;
import io.streamthoughts.jikkou.api.config.Configurable;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.extensions.Extension;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasMetadataAcceptable;
import io.streamthoughts.jikkou.api.selector.ResourceSelector;
import io.streamthoughts.jikkou.common.annotation.AnnotationResolver;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to collect all resources that currently exist into the managed system.
 * An {@link ResourceCollector} is only responsible to collect resources for a specific type.
 *
 * @param <R> type of the resources that are collected.
 */
@Evolving
@ExtensionType("Collector")
public interface ResourceCollector<R extends HasMetadata>
        extends HasMetadataAcceptable, Extension, Configurable {

    /**
     * Gets all the resources that exist into the managed system.
     *
     * @param configuration the configuration settings that may be used to get resources.
     * @return the list of resources.
     */
    default List<R> listAll(@NotNull Configuration configuration) {
        return listAll(configuration, Collections.emptyList());
    }

    /**
     * Gets all the resources that exist into the managed system. The given selectors
     * can be used as predicates to only select a subset of resources.
     *
     * @param configuration the configuration settings that may be used to get resources.
     * @param selectors     the selectors to be used for filtering the resource to describe.
     * @return the list of resources.
     */
    List<R> listAll(@NotNull Configuration configuration, @NotNull List<ResourceSelector> selectors);

    /**
     * Gets all the resources that exist on the remote system and that math the given selectors.
     *
     * @param selectors the selector to be used for filtering the resource to describe.
     * @return the list of resources.
     */
    default List<R> listAll(@NotNull List<ResourceSelector> selectors) {
        List<ConfigPropertyDescriptor> descriptors = getConfigPropertyDescriptors(this);
        Map<String, String> defaultConfigProperties = descriptors
                .stream()
                .collect(Collectors.toMap(ConfigPropertyDescriptor::name, ConfigPropertyDescriptor::defaultValue));
        return listAll(Configuration.from(defaultConfigProperties), selectors);
    }

    /**
     * Gets all the resources that exist into the managed system.
     *
     * @return the list of resources.
     */
    default List<R> listAll() {
        return listAll(Collections.emptyList());
    }

    static List<ConfigPropertyDescriptor> getConfigPropertyDescriptors(ResourceCollector<?> collector) {
        return getConfigPropertyDescriptors(collector.getClass());
    }

    static List<ConfigPropertyDescriptor> getConfigPropertyDescriptors(Class<? extends ResourceCollector> type) {
        return AnnotationResolver
                .findAllAnnotationsByType(type, AcceptsConfigProperty.class)
                .stream()
                .map(descriptor -> new ConfigPropertyDescriptor(
                        descriptor.name(),
                        descriptor.type(),
                        descriptor.description(),
                        descriptor.defaultValue(),
                        descriptor.isRequired()
                ))
                .toList();
    }
}
