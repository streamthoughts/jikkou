/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension.annotations;

import static java.lang.annotation.ElementType.TYPE;

import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Inherited
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface Category {

    /**
     * Gets the name of the category to which this extension belongs to.
     * @return the name of the category.
     */
    ExtensionCategory value();

}
