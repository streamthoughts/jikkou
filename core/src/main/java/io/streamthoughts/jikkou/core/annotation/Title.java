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
 * Used to provide the short description of an element.
 *
 * @see io.streamthoughts.jikkou.core.extension.Extension
 * @see io.streamthoughts.jikkou.core.models.Resource
 */
@Documented
@Inherited
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface Title {

    /**
     * @return The short description of current element.
     */
    String value();
}
