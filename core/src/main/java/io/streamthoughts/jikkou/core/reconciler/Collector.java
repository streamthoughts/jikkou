/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.annotations.Category;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasMetadataAcceptable;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.core.selector.Selectors;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Service interface for listing the resources available on a specific managed system.
 * A collector implementation can only collect resources of the same type.
 *
 * @param <R> type of the resources that are collected.
 */
@Evolving
@Enabled
@Category(ExtensionCategory.COLLECTOR)
public interface Collector<R extends HasMetadata>
        extends HasMetadataAcceptable, Extension {

    /**
     * Gets a single resource by name.
     * <p>
     * The {@link Collector} interface provides a default and non-optimized implementation
     * which applies a filter on the return of the method {@link #listAll(Configuration, Selector)}.
     *
     * @param name          The resource's name.
     * @param configuration The configuration settings that may be used to get resource.
     * @return The optional resource.
     */
    default Optional<R> get(@NotNull String name,
                            @NotNull Configuration configuration) {
        return listAll(configuration, Selectors.NO_SELECTOR).stream()
                .filter(resource -> {
                    ObjectMeta metadata = resource.getMetadata();
                    if (metadata == null) return false;
                    return name.equalsIgnoreCase(metadata.getName());
                })
                .findFirst();
    }

    /**
     * Gets all the resources that exist into the managed system. The given selectors
     * can be used as predicates to only select a subset of resources.
     *
     * @param configuration the configuration settings that may be used to get resources.
     * @param selector      the selector to be used for filtering the resource to describe.
     * @return the list of resources.
     */
    ResourceList<R> listAll(@NotNull Configuration configuration,
                            @NotNull Selector selector);

}
