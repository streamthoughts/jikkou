/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to inform users of how much to rely on a particular package,
 * class or method not changing over time. The stability can be
 * {@link Stable}, {@link Evolving} or {@link Unstable}. <br>
 *
 * 1. {@link Stable} means compatibility can break only at major release (m.0)
 * 2. {@link Evolving} means compatibility can break at minor release (m.x)
 * 3. {@link Unstable} means compatibility can break at any release
 */
@InterfaceStability.Evolving
public class InterfaceStability {
    /**
     * Stable, can evolve while retaining compatibility for minor
     * release boundaries; but compatibility may be broken.
     *
     * This is the default stability level for public APIs that are not annotated.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Stable { }

    /**
     * Evolving, but compatibility may be broken at minor release (i.e. m.x).
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Evolving { }

    /**
     * No guarantee is provided as to reliability or stability across any
     * level of release granularity.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Unstable { }
}