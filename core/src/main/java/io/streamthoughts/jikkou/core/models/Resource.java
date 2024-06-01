/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.common.annotation.AnnotationResolver;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.annotation.Transient;
import io.streamthoughts.jikkou.core.resource.ResourceDeserializer;
import java.io.Serializable;

@Evolving
@JsonDeserialize(using = ResourceDeserializer.class)
@Reflectable
public interface Resource extends Serializable {

    /**
     * Gets resource api version.
     *
     * @return the API Version of this resource.
     */
    default String getApiVersion() {
        return getApiVersion(this.getClass());
    }

    /**
     * Gets resource kind.
     *
     * @return the kind of this resource.
     */
    default String getKind() {
        return getKind(this.getClass());
    }

    /**
     * Check whether this resource should not be part of the reconciliation process.
     *
     * @return {@link true} if this class is annotated with {@link Transient}, otherwise return {@link false}.
     */
    static boolean isTransient(final Class<?> clazz) {
        return AnnotationResolver.isAnnotatedWith(clazz, Transient.class);
    }

    /**
     * Gets the Version of the given resource class.
     *
     * @param clazz the resource class for which to extract the Version.
     * @return the Version or {@code null}.
     */
    static String getApiVersion(final Class<?> clazz) {
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
     * @return the Kind or {@code null}.
     */
    static String getKind(final Class<?> clazz) {
        Kind kind = clazz.getAnnotation(Kind.class);
        if (kind != null) {
            return kind.value();
        }
        return clazz.getSimpleName();
    }

}
