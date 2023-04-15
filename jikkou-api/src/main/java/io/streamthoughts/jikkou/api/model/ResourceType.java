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
package io.streamthoughts.jikkou.api.model;

import java.util.Objects;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public final class ResourceType implements HasMetadataAcceptable {
    private final String kind;
    private final String group;
    private final String apiVersion;
    private final boolean isTransient;

    public static ResourceType create(@NotNull HasMetadata resource) {
        return create(resource.getClass());
    }

    public static ResourceType create(@NotNull Class<? extends HasMetadata> resource) {
        return create(
                HasMetadata.getKind(resource),
                HasMetadata.getApiVersion(resource),
                HasMetadata.isTransient(resource)
        );
    }

    public static ResourceType create(@NotNull final String kind,
                                      @Nullable final String apiVersion) {
        return create(kind, apiVersion, false);
    }

    public static ResourceType create(@NotNull final String kind,
                                      @Nullable final String apiVersion,
                                      final boolean isTransient) {
        Objects.requireNonNull(kind, "'kind' should not be null");
        if (apiVersion == null) {
            return new ResourceType(kind, null, null, isTransient);
        } else {
            String[] versionParts = new String[]{null, apiVersion};
            if (apiVersion.contains("/")) {
                versionParts = apiVersion.split("/", 2);
            }
            return new ResourceType(kind, versionParts[0], versionParts[1], isTransient);
        }
    }

    /**
     * Creates a new {@link ResourceType} instance.
     *
     * @param kind      the kind of the resource.
     * @param group     the group of the resource.
     * @param version   the version of the resource.
     */
    public ResourceType(@NotNull final String kind,
                        @Nullable final String group,
                        @Nullable final String version) {
        this(kind, group, version, false);
    }

    /**
     * Creates a new {@link ResourceType} instance.
     *
     * @param kind      the kind of the resource.
     * @param group     the group of the resource.
     * @param version   the version of the resource.
     */
    public ResourceType(@NotNull final String kind,
                        @Nullable final String group,
                        @Nullable final String version,
                        final boolean isTransient) {
        this.kind = Objects.requireNonNull(kind, "'kind' cannot be null");
        this.group = group;
        this.apiVersion = version;
        this.isTransient = isTransient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAccept(@NotNull HasMetadata resource) {
        return canAccept(ResourceType.create(resource));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAccept(@NotNull ResourceType that) {
        if (equals(that)) {
            return true;
        }

        if (that.apiVersion != null && this.apiVersion != null) {
            return Objects.equals(kind, that.kind) &&
                   Objects.equals(apiVersion, that.apiVersion);
        }

        return Objects.equals(kind, that.kind);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceType that = (ResourceType) o;
        return isTransient == that.isTransient
                && Objects.equals(kind, that.kind)
                && Objects.equals(group, that.group)
                && Objects.equals(apiVersion, that.apiVersion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(kind, group, apiVersion, isTransient);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" +
                "kind=" + kind +
                ", group=" + group +
                ", apiVersion=" + apiVersion +
                ", transient=" + isTransient +
                ']';
    }
}
