/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.api.selector;

import io.streamthoughts.jikkou.api.model.HasMetadata;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class AggregateSelector implements ResourceSelector {

    private final List<? extends ResourceSelector> selectors;

    /**
     * Creates a new {@link AggregateSelector} instance.
     *
     * @param selectors the list of {@link ResourceSelector}.
     */
    public AggregateSelector(List<? extends ResourceSelector> selectors) {
        this.selectors = Objects.requireNonNull(selectors, "selectors must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean apply(@NotNull HasMetadata resource) {
        return selectors.stream().allMatch(selector -> selector.apply(resource));
    }
}
