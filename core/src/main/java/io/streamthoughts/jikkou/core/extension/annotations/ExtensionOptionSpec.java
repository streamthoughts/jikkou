/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension.annotations;

import static java.lang.annotation.ElementType.TYPE;

import io.streamthoughts.jikkou.core.config.ConfigPropertySpec;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionOptionSpec {

    /**
     * The name of this property.
     *
     * @return the string name.
     */
    String name();

    /**
     * The description of this property.
     *
     * @return the string description.
     */
    String description() default ConfigPropertySpec.NO_DEFAULT_VALUE;

    /**
     * The default value of this property.
     *
     * @return the string representation of the default value.
     */
    String defaultValue() default ConfigPropertySpec.NO_DEFAULT_VALUE;

    /**
     * The type of this property.
     *
     * @return The type of this option
     */
    Class<?> type();

    /**
     * Specifies if the property is required.
     *
     * @return {@code true} if the property is required, otherwise {@code false}.
     */
    boolean required() default false;
}
