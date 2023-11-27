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
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.action.Action;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.core.models.HasName;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.reconcilier.Collector;
import io.streamthoughts.jikkou.core.reconcilier.Controller;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.core.validation.Validation;
import org.jetbrains.annotations.NotNull;

/**
 * The top-level interface for extension.
 *
 * @see Action
 * @see Resource
 * @see Validation
 * @see Transformation
 * @see Controller
 * @see Collector
 * @see HealthIndicator
 */
@Evolving
public interface Extension extends HasName {

    /**
     * Initializes this extension with the specified context.
     * 
     * This method is invoked each time the extension is used. Note that the given context is tied
     * to this extension and therefore cannot be passed on to another extension through this method.
     *
     * @param context The extension context.
     */
    default void init(@NotNull ExtensionContext context) {
        // intentionally left blank
    }
}
