/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler.annotations;

import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ControllerConfiguration {


    /**
     * Specifies the reconciliation modes which are supported accepted by the controller.
     * An empty set implies that the controller can accept any reconciliation mode.
     */
    ReconciliationMode[] supportedModes() default {};

    SupportedResource[] supportChanges() default {};
}
