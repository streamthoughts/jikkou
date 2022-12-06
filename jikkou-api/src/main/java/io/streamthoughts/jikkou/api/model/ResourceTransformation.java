/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.api.model;

import io.streamthoughts.jikkou.api.config.Configurable;
import io.streamthoughts.jikkou.api.extensions.Extension;
import io.streamthoughts.jikkou.api.extensions.annotations.ExtensionType;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import org.jetbrains.annotations.NotNull;

/**
 * Transform an input {@link HasMetadata} into one ore multiple {@link HasMetadata}.
 */
@InterfaceStability.Evolving
@ExtensionType("transformation")
public interface ResourceTransformation extends
        HasMetadataAcceptable,
        Extension,
        Configurable {

    default String name() {
        final String className = this.getClass().getSimpleName();
        return className.replaceAll("Transformation", "");
    }

    /**
     * Applies this transformation on the given {@link HasMetadata} object.
     *
     * @param resource    the {@link HasMetadata} to transform.
     * @param list        the {@link ResourceList} involved in the current operation.
     *
     * @return            the list of resources resulting from that transformation.
     */
    @NotNull HasMetadata transform(@NotNull HasMetadata resource, @NotNull ResourceList list);

}
