/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.api.transform;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.annotations.AcceptsResources;
import io.streamthoughts.jikkou.api.annotations.ExtensionEnabled;
import io.streamthoughts.jikkou.api.annotations.Priority;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasPriority;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

@Priority(HasPriority.HIGHEST_PRECEDENCE)
@ExtensionEnabled
@AcceptsResources(value = {})
public class ExcludeIgnoreResourceTransformation implements ResourceTransformation<HasMetadata> {

    /** {@inheritDoc}**/
    @Override
    public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata toTransform,
                                                    @NotNull HasItems resources) {
        return JikkouMetadataAnnotations.isAnnotatedWithIgnore(toTransform) ?
                Optional.empty() : Optional.of(toTransform);
    }
}
