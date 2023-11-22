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
package io.streamthoughts.jikkou.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

/**
 * ResourceListRequest.
 *
 * @param options   The parameters.
 * @param selectors The list of selector expressions.
 */
public record ResourceListRequest(@Nullable @JsonProperty("options") Map<String, Object> options,
                                  @Nullable @JsonProperty("selectors") List<String> selectors) {

    /**
     * Creates a new {@link ResourceListRequest} instance.
     *
     * @param options   The parameters.
     * @param selectors The selector expression.
     */
    @ConstructorProperties({
            "options",
            "selectors"
    })
    public ResourceListRequest {
    }

    /**
     * Creates a new {@link ResourceListRequest} instance.
     *
     * @param options   The parameters.
     */
    public ResourceListRequest(Map<String, Object> options) {
        this(options, null);
    }

    /**
     * Creates a new {@link ResourceListRequest} instance.
     */
    public ResourceListRequest() {
        this(null, null);
    }

    public ResourceListRequest options(Map<String, ?> options) {
        Map<String, Object> map = new HashMap<>(options());
        map.putAll(options);
        return new ResourceListRequest(map, selectors);
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
    public Map<String, Object> options() {
        return Optional.ofNullable(options).orElse(Collections.emptyMap());
    }
}
