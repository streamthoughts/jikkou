/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.action;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.streamthoughts.jikkou.common.utils.Enums;
import org.jetbrains.annotations.Nullable;

/**
 * Status of the execution of an action.
 */
public enum ExecutionStatus {
    /**
     * Action was executed successfully.
     **/
    SUCCEEDED,
    /**
     * Action execution has timed out.
     */
    TIMED_OUT,
    /**
     * Action was executed with one or more errors.
     **/
    FAILED;

    @JsonCreator
    public static ExecutionStatus getForNameIgnoreCase(final @Nullable String str) {
        return Enums.getForNameIgnoreCase(str, ExecutionStatus.class);
    }
}
