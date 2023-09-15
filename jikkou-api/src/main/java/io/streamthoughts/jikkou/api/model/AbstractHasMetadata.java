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
package io.streamthoughts.jikkou.api.model;

import java.util.Objects;

public abstract class AbstractHasMetadata implements HasMetadata {

    private final String kind;
    private final String apiVersion;
    private final ObjectMeta metadata;

    public AbstractHasMetadata(String kind,
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractHasMetadata that = (AbstractHasMetadata) o;
        return Objects.equals(kind, that.kind) &&
                Objects.equals(apiVersion, that.apiVersion) &&
                Objects.equals(metadata, that.metadata);
    }
    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(kind, apiVersion, metadata);
    }
}