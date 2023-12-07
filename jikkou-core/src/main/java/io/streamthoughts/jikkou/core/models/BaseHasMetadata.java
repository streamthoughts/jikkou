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

import java.util.Optional;

public class BaseHasMetadata implements HasMetadata {

    private String kind;
    private String apiVersion;
    private ObjectMeta metadata;

    /**
     * Creates a new {@link BaseHasMetadata} instance.
     */
    public BaseHasMetadata() {
        this(new ObjectMeta());
    }

    /**
     * Creates a new {@link BaseHasMetadata} instance.
     *
     * @param metadata The object metadata.
     */
    public BaseHasMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
    }

    /**
     * Creates a new {@link BaseHasMetadata} instance.
     *
     * @param kind       The resource kind.
     * @param apiVersion The resource API Version.
     * @param metadata   The object metadata..
     */
    public BaseHasMetadata(String kind,
                           String apiVersion,
                           ObjectMeta metadata) {
        this.kind = kind;
        this.apiVersion = apiVersion;
        this.metadata = metadata;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String getKind() {
        return Optional.of(kind).orElse(Resource.getKind(this.getClass()));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String getApiVersion() {
        return Optional.of(apiVersion).orElse(Resource.getApiVersion(this.getClass()));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ObjectMeta getMetadata() {
        return metadata;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public HasMetadata withMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
        return this;
    }
}
