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

/**
 * Interface for constructing new resources.
 *
 * @param <T> type of the resource.
 * @param <S> type of the specification.
 */
public interface HasSpecBuilder<T extends HasMetadata, S> {

    /**
     * Gets the resource API Version.
     *
     * @return the API Version.
     */
    String apiVersion();

    /**
     * Sets the resource ApiVersion.
     *
     * @param apiVersion the API Version.
     * @return {@code this} builder.
     */
    HasSpecBuilder<T, S> withApiVersion(String apiVersion);

    /**
     * Sets the resource Kind.
     *
     * @param kind the Kind.
     * @return {@code this} builder.
     */
    HasSpecBuilder<T, S> withKind(String kind);

    /**
     * Gets the resource Kind.
     *
     * @return the Kind.
     */
    String kind();

    /**
     * Sets the resource metadata.
     *
     * @param metadata The metadata.
     * @return {@code this} builder.
     */
    HasSpecBuilder<T, S> withMetadata(ObjectMeta metadata);

    /**
     * Gets the resource metadata.
     *
     * @return The {@link ObjectMeta}.
     */
    ObjectMeta metadata();

    /**
     * Gets the resource specification.
     *
     * @return The spec.
     */
    S spec();

    /**
     * Sets the resource specification.
     *
     * @param spec The spec.
     * @return {@code this} builder.
     */
    HasSpecBuilder<T, S> withSpec(S spec);

    /**
     * Creates a new resource.
     *
     * @return The new resource.
     */
    T build();
}
