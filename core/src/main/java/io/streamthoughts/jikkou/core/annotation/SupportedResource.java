/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.annotation;

import static java.lang.annotation.ElementType.TYPE;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the resource type supported by the extension.
 */
@Documented
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SupportedResources.class)
public @interface SupportedResource {
    /**
     * @return The supported resource kind.
     */
    String kind() default "";
    /**
     * @return The supported API Version.
     */
    String apiVersion() default "";
    /**
     * @return The supported type. Can be null if kind, apiVersion or both have been defined.
     */
    Class<? extends HasMetadata> type() default HasMetadata.class;
}
