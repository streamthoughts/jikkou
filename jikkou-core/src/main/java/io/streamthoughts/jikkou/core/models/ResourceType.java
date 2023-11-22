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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represent a resource type.
 *
 * @param kind          The resource Kind.
 * @param group         The resource API Group.
 * @param apiVersion    The resource API Version.
 * @param isTransient   Specify whether the resource is transient.
 */
@JsonPropertyOrder({
        "kind",
        "group",
        "apiVersion",
        "isTransient"
})
@Reflectable
public record ResourceType(@JsonProperty("kind") @NotNull String kind,
                           @JsonProperty("group") @Nullable String group,
                           @JsonProperty("apiVersion") @Nullable String apiVersion,
                           @JsonProperty("isTransient") boolean isTransient) implements HasMetadataAcceptable {

    @ConstructorProperties({
            "kind",
            "group",
            "apiVersion",
            "isTransient"
    })
    public ResourceType {
        Objects.requireNonNull(kind, "'kind' cannot be null");
    }

    /**
     * Creates a new {@link ResourceType} instance.
     */
    public ResourceType(@NotNull String kind,
                        @Nullable String group,
                        @Nullable String apiVersion) {
        this(kind, group, apiVersion, false);
    }

    /**
     * Gets a new {@link ResourceType} with the given version.
     *
     * @param version the resource version.
     * @return a new {@link ResourceType}.
     */
    public ResourceType version(String version) {
        return ResourceType.of(kind, version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAccept(@NotNull ResourceType that) {
        if (equals(that)) {
            return true;
        }

        if (that.group != null && this.group != null) {
            return Objects.equals(group, that.group) &&
                    Objects.equals(kind, that.kind) &&
                    Objects.equals(apiVersion, that.apiVersion);
        }

        if (that.apiVersion != null && this.apiVersion != null) {
            return Objects.equals(kind, that.kind) &&
                    Objects.equals(apiVersion, that.apiVersion);
        }

        return Objects.equals(kind, that.kind);
    }

    /**
     * Static helper method to create a new {@link ResourceType} from the given resource.
     *
     * @param resource the resource.
     * @return a new {@link ResourceType}.
     */
    public static ResourceType of(@NotNull Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("resource cannot be null");
        }
        return of(
                resource.getKind(),
                resource.getApiVersion(),
                Resource.isTransient(resource.getClass())
        );
    }

    /**
     * Static helper method to create a new {@link ResourceType} from the given resource.
     *
     * @param resource the resource.
     * @return a new {@link ResourceType}.
     */
    public static ResourceType of(@NotNull Class<? extends Resource> resource) {
        if (resource == null) {
            throw new IllegalArgumentException("resource cannot be null");
        }
        return of(
                Resource.getKind(resource),
                Resource.getApiVersion(resource),
                Resource.isTransient(resource)
        );
    }

    /**
     * Static helper method to create a new {@link ResourceType} for the given Kind and ApiVersion.
     *
     * @param kind       the resource Kind.
     * @param apiVersion the resource ApiVersion.
     * @return a new {@link ResourceType}.
     */
    public static ResourceType of(@NotNull final String kind,
                                  @Nullable final String apiVersion) {
        return of(kind, apiVersion, false);
    }

    /**
     * Static helper method to create a new {@link ResourceType} for the given Kind .
     *
     * @param kind the resource Kind.
     * @return a new {@link ResourceType}.
     */
    public static ResourceType of(@NotNull final String kind) {
        return of(kind, null, false);
    }

    /**
     * Static helper method to create a new {@link ResourceType} for the given Kind and ApiVersion.
     *
     * @param kind        The resource Kind.
     * @param apiVersion  The resource API version.
     * @param isTransient flag indicating if the resource is transient.
     * @return a new {@link ResourceType}.
     */
    public static ResourceType of(@NotNull final String kind,
                                  @Nullable final String apiVersion,
                                  final boolean isTransient) {
        Objects.requireNonNull(kind, "'kind' should not be null");
        if (Strings.isBlank(apiVersion)) {
            return new ResourceType(kind, null, null, isTransient);
        } else {
            String[] versionParts = new String[]{null, apiVersion};
            if (apiVersion.contains("/")) {
                versionParts = apiVersion.split("/", 2);
            }
            return new ResourceType(kind, versionParts[0], versionParts[1], isTransient);
        }
    }
}
