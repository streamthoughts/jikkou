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
package io.streamthoughts.jikkou.api;

import io.streamthoughts.jikkou.api.model.ResourceList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class ResourceListHandlers implements ResourceListHandler {

    private final List<ResourceListHandler> handlerList;

    /**
     * Creates a new {@link ResourceListHandlers} instance.
     *
     * @param handlerList the handler list.
     */
    public ResourceListHandlers(final @NotNull List<ResourceListHandler> handlerList) {
        Objects.requireNonNull(handlerList, "handlerList cannot be null");
        this.handlerList = Collections.unmodifiableList(handlerList);
    }

    /** {@inheritDoc} **/
    @Override
    public @NotNull ResourceList handle(@NotNull ResourceList resources) {
        ResourceList handled = resources;
        for (ResourceListHandler handler : handlerList) {
            handled = handler.handle(handled);
        }
        return handled;
    }
}
