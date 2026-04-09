/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.annotation;

import static java.lang.annotation.ElementType.TYPE;

import io.jikkou.core.models.HasPriority;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the reconciliation order for a resource type within its provider.
 * <p>
 * Resource types with lower order values are reconciled first during creation
 * and last during deletion. This allows providers to declare dependencies
 * between their own resource types (e.g., Iceberg Namespace before Table).
 * <p>
 * Cross-provider ordering is controlled by Provider Groups configuration,
 * not by this annotation.
 *
 * @see HasPriority#NO_ORDER
 * @since 0.39.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface ReconciliationOrder {
    /**
     * The order value. Lower values are reconciled first.
     * Default is {@link HasPriority#NO_ORDER}.
     */
    int value() default HasPriority.NO_ORDER;
}
