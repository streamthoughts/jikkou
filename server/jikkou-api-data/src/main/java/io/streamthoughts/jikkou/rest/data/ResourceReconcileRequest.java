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
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.selector.SelectorMatchingStrategy;
import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * ResourceReconcileRequest.
 *
 * @param params    the query parameters.
 * @param resources the resource entities.
 */
public record ResourceReconcileRequest(
        @Nullable @JsonProperty("params") Params params,
        @Nullable @JsonProperty("resources") List<? extends HasMetadata> resources) {

    @ConstructorProperties({
            "params",
            "resources"
    })
    public ResourceReconcileRequest {
    }

    // Empty constructor
    public ResourceReconcileRequest() {
        this(null, null);
    }

    @Override
    public Params params() {
        return Optional.ofNullable(params).orElse(new Params());
    }

    @Override
    public List<? extends HasMetadata> resources() {
        return Optional.ofNullable(resources).orElse(Collections.emptyList());
    }

    /**
     * Params.
     *
     * @param annotations the key/value annotations.
     * @param labels      the key/value labels.
     * @param options     the query options.
     * @param selectors   list of selector expressions used for including or excluding resources.
     */
    public record Params(
            @Nullable @JsonProperty("annotations") Map<String, Object> annotations,
            @Nullable @JsonProperty("labels") Map<String, Object> labels,
            @Nullable @JsonProperty("options") Map<String, Object> options,
            @Nullable @JsonProperty("selectors") List<String> selectors,
            @Nullable @JsonProperty("selectors_match") SelectorMatchingStrategy selectorMatchingStrategy) {

        @ConstructorProperties({
                "annotations",
                "labels",
                "configuration",
                "selectors"
        })
        public Params {
        }

        /**
         * Creates a new {@link Params} instance.
         */
        public Params() {
            this(null, null, null, null, null);
        }

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

        @Override
        public Map<String, Object> annotations() {
            return Optional.ofNullable(annotations).orElse(Collections.emptyMap());
        }

        @Override
        public Map<String, Object> labels() {
            return Optional.ofNullable(labels).orElse(Collections.emptyMap());
        }

        @Override
        public Map<String, Object> options() {
            return Optional.ofNullable(options).orElse(Collections.emptyMap());
        }
    }

}
