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

import io.streamthoughts.jikkou.api.model.annotations.ApiVersion;
import io.streamthoughts.jikkou.api.model.annotations.Kind;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.util.Optional;

@InterfaceStability.Evolving
public interface HasMetadata extends Resource {

    /**
     * @return  the metadata of this resource object.
     */
    ObjectMeta getMetadata();

    HasMetadata withMetadata(ObjectMeta objectMeta);

    /**
     * @return  the API Version of this resource.
     */
    String getApiVersion();

    /**
     * @return  the kind of this resource.
     */
    String getKind();

    /**
     * Gets the Version of the given resource class.
     *
     * @param clazz the resource class for which to extract the Version.
     * @return      the Version or {@code null}.
     */
    static String getApiVersion(final Class<? extends Resource> clazz) {
        ApiVersion version = clazz.getAnnotation(ApiVersion.class);
        if (version != null) {
            return version.value();
        }
        return null;
    }

    /**
     * Gets the Kind of the given resource class.
     *
     * @param clazz the resource class for which to extract the Kind.
     * @return      the Kind or {@code null}.
     */
    static String getKind(final Class<? extends Resource> clazz) {
        Kind kind = clazz.getAnnotation(Kind.class);
        if (kind != null) {
            return kind.value();
        }
        return clazz.getSimpleName();
    }

    default Optional<ObjectMeta> optionalMetadata() {
        return Optional.ofNullable(getMetadata());
    }
}
