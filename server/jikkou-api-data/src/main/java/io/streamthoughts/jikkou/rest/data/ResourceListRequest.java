/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.selector.SelectorMatchingStrategy;
import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * ResourceListRequest.
 *
 * @param options   The parameters.
 * @param selectors The list of selector expressions.
 * @param selectorMatchingStrategy The selector matching strategy.
 */
public record ResourceListRequest(@Nullable @JsonProperty("options") Map<String, Object> options,
                                  @Nullable @JsonProperty("selectors") List<String> selectors,
                                  @Nullable @JsonProperty("selectors_match") SelectorMatchingStrategy selectorMatchingStrategy) {

    /**
     * Creates a new {@link ResourceListRequest} instance.
     *
     * @param options   The parameters.
     * @param selectors The selector expression.
     */
    @ConstructorProperties({
            "options",
            "selectors",
            "selectors_match"
    })
    public ResourceListRequest {
    }

    /**
     * Creates a new {@link ResourceListRequest} instance.
     *
     * @param options The parameters.
     */
    public ResourceListRequest(Map<String, Object> options) {
        this(options, null, null);
    }

    /**
     * Creates a new {@link ResourceListRequest} instance.
     */
    public ResourceListRequest() {
        this(null, null, null);
    }

    public ResourceListRequest options(Map<String, ?> options) {
        Map<String, Object> map = new HashMap<>(options());
        map.putAll(options);
        return new ResourceListRequest(map, selectors, selectorMatchingStrategy);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<String> selectors() {
        return Optional.ofNullable(selectors).orElse(Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public SelectorMatchingStrategy selectorMatchingStrategy() {
        return Optional.ofNullable(selectorMatchingStrategy).orElse(SelectorMatchingStrategy.ALL);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Map<String, Object> options() {
        return Optional.ofNullable(options).orElse(Collections.emptyMap());
    }
}
