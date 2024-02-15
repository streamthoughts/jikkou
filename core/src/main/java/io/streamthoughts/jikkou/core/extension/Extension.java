/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.action.Action;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.core.models.HasName;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.reconciler.Controller;
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
