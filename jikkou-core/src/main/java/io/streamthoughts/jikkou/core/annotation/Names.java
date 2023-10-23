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