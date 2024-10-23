/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.annotation;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to be used on classes that implement the {@link io.streamthoughts.jikkou.spi.ExtensionProvider}.
 */
@Documented
@Inherited
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface Provider {

    /**
     * Gets a name for this provider.
     *
     * @return The provider name.
     */
    String name();

    /**
     * Gets a short description for this provider.
     *
     * @return The provider description.
     */
    String description() default "";

    /**
     * Gets the tags for this provider.
     *
     * @return The list of tags.
     */
    String[] tags() default {};

    /**
     * Gets the URL for the target documentation. This MUST be in the form of a URL.
     *
     * @return The URL.
     */
    String externalDocs() default "";
}
