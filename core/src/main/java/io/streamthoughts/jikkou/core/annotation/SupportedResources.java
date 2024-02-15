/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.annotation;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a set of resources type that are supported by an extension.
 * An empty set implies that the extension supports any resource-type or takes care of checking resource-type itself.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface SupportedResources {
    /**
     * @return The list of resources supported the extension.
     */
    SupportedResource[] value() default {};
}
