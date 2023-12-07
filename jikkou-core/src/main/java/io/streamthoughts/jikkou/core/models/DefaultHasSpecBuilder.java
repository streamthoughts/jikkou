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
package io.streamthoughts.jikkou.core.models;

import java.util.function.Function;

/**
 * Default class for constructing new resource objects?
 *
 * @param <T> type of the resource.
 * @param <S> Type of the spec.
 */
public final class DefaultHasSpecBuilder<T extends HasMetadata, S> implements HasSpecBuilder<T, S> {

    private ObjectMeta metadata = null;
    private String apiVersion;
    private String kind;
    private S spec;

    private final Function<DefaultHasSpecBuilder<T, S>, T> builder;

    /**
     * Creates a new {@link DefaultHasSpecBuilder} instance.
     *
     * @param builder the builder function.
     */
    public DefaultHasSpecBuilder(Function<DefaultHasSpecBuilder<T, S>, T> builder) {
        this(null, null, builder);
    }

    /**
     * Creates a new {@link DefaultHasSpecBuilder} instance.
     *
     * @param apiVersion the API Version.
     * @param kind       the Kind.
     * @param builder    the builder function.
     */
    public DefaultHasSpecBuilder(String apiVersion,
                                 String kind,
                                 Function<DefaultHasSpecBuilder<T, S>, T> builder) {
        this.kind = kind;
        this.apiVersion = apiVersion;
        this.builder = builder;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultHasSpecBuilder<T, S> withMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ObjectMeta metadata() {
        return metadata;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultHasSpecBuilder<T, S> withSpec(S spec) {
        this.spec = spec;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public S spec() {
        return spec;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String apiVersion() {
        return apiVersion;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultHasSpecBuilder<T, S> withApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultHasSpecBuilder<T, S> withKind(String kind) {
        this.kind = kind;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String kind() {
        return kind;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public T build() {
        return builder.apply(this);
    }
}
