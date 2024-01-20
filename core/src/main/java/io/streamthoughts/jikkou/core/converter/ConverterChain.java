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
package io.streamthoughts.jikkou.core.converter;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public final class ConverterChain implements Converter<HasMetadata, HasMetadata> {

    private static final Logger LOG = LoggerFactory.getLogger(ConverterChain.class);

    private final List<Converter> converters;

    /**
     * Creates a new {@link ConverterChain} instance.
     *
     * @param converters The Converters.
     */
    public ConverterChain(List<? extends Converter> converters) {
        this.converters = Collections.unmodifiableList(converters);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @SuppressWarnings("unchecked")
    public @NotNull List<HasMetadata> apply(@NotNull HasMetadata resource) {
        if (converters.isEmpty()) return List.of(resource);

        List<Converter> accepting = findMatchingConverters(resource);

        if (accepting.isEmpty()) {
            return List.of(resource);
        }

        if (accepting.size() > 1) {
            accepting = accepting.stream().sorted(Comparator.comparing(Converter::getPriority)).toList();
            if (LOG.isDebugEnabled()) {
                ResourceType type = ResourceType.of(resource);
                LOG.debug("Found multiple converters matching resource of type: group={}, version={} and kind={}. Only first one will be applied: {}",
                        type.group(),
                        type.apiVersion(),
                        type.kind(),
                        accepting.stream().map(it -> it.getName()).collect(Collectors.toList())

                );
            }
        }
        Converter converter = accepting.get(0); // get first converter
        List<HasMetadata> converted = converter.apply(resource);
        return new ConverterChain(getConvertersExcept(converter)).apply(converted);
    }

    public @NotNull List<HasMetadata> apply(@NotNull List<HasMetadata> resources) {
        return resources.stream().map(this::apply).flatMap(Collection::stream).toList();
    }

    private List<Converter> getConvertersExcept(@NotNull Converter converter) {
        return converters.stream()
                .filter(Predicate.not(Predicate.isEqual(converter)))
                .toList();
    }

    private List<Converter> findMatchingConverters(@NotNull HasMetadata resource) {
        return converters.stream()
                .sorted(Comparator.comparing(Converter::getPriority))
                .filter(converter -> canAccept(ResourceType.of(resource)))
                .filter(converter -> canAccept(resource))
                .toList();
    }
}
