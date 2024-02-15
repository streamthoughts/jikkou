/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;


import com.fasterxml.jackson.annotation.JsonCreator;
import io.streamthoughts.jikkou.common.utils.Enums;
import org.jetbrains.annotations.Nullable;

public enum ExtensionCategory {

    /**
     * For any class that implements {@link io.streamthoughts.jikkou.core.validation.Validation}.
     */
    VALIDATION,
    /**
     * For any class that implements {@link io.streamthoughts.jikkou.core.transform.Transformation}.
     */
    TRANSFORMATION,
    /**
     * For any class that implements {@link io.streamthoughts.jikkou.core.reconciler.Controller}.
     */
    CONTROLLER,
    /**
     * For any class that implements {@link io.streamthoughts.jikkou.core.converter.Converter}.
     */
    CONVERTER,
    /**
     * For any class that implements {@link io.streamthoughts.jikkou.core.reconciler.Collector}.
     */
    COLLECTOR,
    /**
     * For any class that implements {@link io.streamthoughts.jikkou.core.reporter.ChangeReporter}.
     */
    REPORTER,
    /**
     * For any class that implements {@link io.streamthoughts.jikkou.core.health.HealthIndicator}.
     */
    HEALTH_INDICATOR,

    /**
     * For any class that implements {@link io.streamthoughts.jikkou.core.action.Action}.
     */
    ACTION,
    /**
     * Any.
     */
    EXTENSION;

    @JsonCreator
    public static ExtensionCategory getForNameIgnoreCase(final @Nullable String str) {
        return Enums.getForNameIgnoreCase(str, ExtensionCategory.class);
    }

}
