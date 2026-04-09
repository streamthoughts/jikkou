/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.extension;

import io.jikkou.common.annotation.InterfaceStability.Evolving;
import io.jikkou.core.action.Action;
import io.jikkou.core.annotation.Reflectable;
import io.jikkou.core.health.HealthIndicator;
import io.jikkou.core.models.HasConfig;
import io.jikkou.core.models.HasName;
import io.jikkou.core.models.Resource;
import io.jikkou.core.reconciler.Collector;
import io.jikkou.core.reconciler.Controller;
import io.jikkou.core.transform.Transformation;
import io.jikkou.core.validation.Validation;
import org.jetbrains.annotations.NotNull;

/**
 * The top-level interface for extensions.
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
@Reflectable
public interface Extension extends HasName, HasConfig {

    /**
     * Initializes this extension with the specified context.
     * <p>
     * This method is invoked each time the extension is used. Note that the given context is tied
     * to this extension and therefore cannot be passed on to another extension through this method.
     *
     * @param context The extension context.
     */
    default void init(@NotNull ExtensionContext context) {
        // intentionally left blank
    }


}
