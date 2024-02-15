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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to provide a code example of an element.
 *
 * @see io.streamthoughts.jikkou.core.extension.Extension
 * @see io.streamthoughts.jikkou.core.models.Resource
 */
@Documented
@Inherited
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({TYPE})
@Repeatable(Examples.class)
public @interface Example {

    /**
     * @return The short description of current example.
     */
    String title() default "";

    /**
     * @return The code of current example.
     */
    String[] code() default "";

    /**
     * Specify whether the example is full, i.e., it contains the 'type' property.
     * @return {@code true} if the example contains the 'type' property. Otherwise {@code false}.
     */
    boolean full() default false;

}
