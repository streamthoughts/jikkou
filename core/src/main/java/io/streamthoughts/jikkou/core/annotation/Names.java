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
import java.lang.annotation.Target;

/**
 *  Names to be used as an alias on the CLI and for display.
 */
@Documented
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface Names {

    /**
     * @return singular name to be used as an alias on the CLI and for display.
     */
    String singular() default "";

    /**
     * @return plural name to be used as an alias on the CLI and for display.
     */
    String plural() default "";

    /**
     * @return short names to be used as an alias on the CLI and for display.
     */
    String[] shortNames() default {};
}