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
package io.streamthoughts.jikkou.core.action;

import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.model.SystemTimeResource;
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
                        .resource(SystemTimeResource.now())
                        .status(ExecutionStatus.SUCCEED)
                        .build()
                )
                .build();
    }
}
