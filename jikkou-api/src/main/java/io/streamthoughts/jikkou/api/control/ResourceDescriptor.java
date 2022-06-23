/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import io.streamthoughts.jikkou.api.ResourceFilter;
import io.streamthoughts.jikkou.api.config.Configurable;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.extensions.Extension;
import io.streamthoughts.jikkou.api.extensions.annotations.ExtensionType;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasMetadataAcceptable;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import org.jetbrains.annotations.NotNull;

@InterfaceStability.Evolving
@ExtensionType("descriptor")
public interface ResourceDescriptor<R extends HasMetadata>
    extends HasMetadataAcceptable, Extension, Configurable, AutoCloseable {

    /**
     * Describes the objects that exist on a remote Kafka cluster.
     *
     * @param configuration the configuration properties to be used for describing resources.
     * @param resourceFilter the filter to be used for filtering the resource to describe.
     * @return the list of resources.
     */
    R describe(@NotNull Configuration configuration, @NotNull ResourceFilter resourceFilter);

    /**
     * Describes the objects that exist on a remote Kafka cluster.
     *
     * @param resourceFilter the filter to be used for filtering the resource to describe.
     * @return the list of resources.
     */
    default R describe(@NotNull ResourceFilter resourceFilter) {
        return describe(Configuration.empty(), resourceFilter);
    }

    /**
     * Describes the objects that exist on a remote Kafka cluster.
     *
     * @return the list of resources.
     */
    default R describe() {
        return describe(Configuration.empty(), ResourceFilter.DEFAULT);
    }

    /** {@inheritDoc} */
    @Override
    default void close() {}
}
