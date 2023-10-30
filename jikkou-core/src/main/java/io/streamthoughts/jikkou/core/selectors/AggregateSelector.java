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
package io.streamthoughts.jikkou.core.selectors;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final class AggregateSelector implements Selector {

    private final List<? extends Selector> selectors;

    /**
     * Creates a new {@link AggregateSelector} instance.
     *
     * @param selectors the list of {@link Selector}.
     */
    public AggregateSelector(List<? extends Selector> selectors) {
        this.selectors = Objects.requireNonNull(selectors, "selectors must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean apply(@NotNull HasMetadata resource) {
        return selectors.stream().allMatch(selector -> selector.apply(resource));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<String> getSelectorExpressions() {
        return selectors.stream()
                .map(Selector::getSelectorExpressions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
