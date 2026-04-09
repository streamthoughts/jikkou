/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.action;

import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.Named;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.model.SystemTimeResource;
import org.jetbrains.annotations.NotNull;

@Named(SystemTimeAction.NAME)
@Title("Returns the current time.")
@Description("The 'SystemTime' action can be used to retrieved the current time from the system clock.")
@SupportedResource(type = SystemTimeResource.class)
public final class SystemTimeAction implements Action<SystemTimeResource> {

    public static final String NAME = "SystemTime";

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull ExecutionResultSet<SystemTimeResource> execute(@NotNull Configuration configuration) {
        return ExecutionResultSet
                .<SystemTimeResource>newBuilder()
                .result(ExecutionResult
                        .<SystemTimeResource>newBuilder()
                        .data(SystemTimeResource.now())
                        .status(ExecutionStatus.SUCCEEDED)
                        .build()
                )
                .build();
    }
}
