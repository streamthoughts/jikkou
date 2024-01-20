/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
