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
package io.streamthoughts.jikkou.core.annotation;

import static java.lang.annotation.ElementType.TYPE;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the resource type that can be handled by an extension.
 */
@Documented
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(HandledResources.class)
public @interface HandledResource {
    String kind() default "";

    String apiVersion() default "";

    Class<? extends HasMetadata> type() default HasMetadata.class;
}
