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
package io.streamthoughts.jikkou.core.reconcilier;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.annotation.Category;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.config.Configurable;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasMetadataAcceptable;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.selectors.Selector;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Service interface for listing the resources available on a specific managed system.
 * A collector implementation can only collect resources of the same type.
 *
 * @param <R> type of the resources that are collected.
 */
@Evolving
@Enabled
@Category("Collector")
public interface Collector<R extends HasMetadata>
        extends HasMetadataAcceptable, Extension, Configurable {

    /**
     * Gets all the resources that exist into the managed system.
     *
     * @param configuration the configuration settings that may be used to get resources.
     * @return the list of resources.
     */
    default ResourceListObject<R> listAll(@NotNull Configuration configuration) {
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
    ResourceListObject<R> listAll(@NotNull Configuration configuration, @NotNull List<Selector> selectors);

    /**
     * Gets all the resources that exist on the remote system and that math the given selectors.
     *
     * @param selectors the selector to be used for filtering the resource to describe.
     * @return the list of resources.
     */
    default ResourceListObject<R> listAll(@NotNull List<Selector> selectors) {
        return listAll(getDefaultConfiguration(), selectors);
    }

    /**
     * Gets all the resources that exist into the managed system.
     *
     * @return the list of resources.
     */
    default ResourceListObject<R> listAll() {
        return listAll(Collections.emptyList());
    }

}
