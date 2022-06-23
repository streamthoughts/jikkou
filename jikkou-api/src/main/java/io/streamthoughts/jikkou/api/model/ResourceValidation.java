/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.api.model;

import io.streamthoughts.jikkou.api.config.Configurable;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.extensions.Extension;
import io.streamthoughts.jikkou.api.extensions.annotations.ExtensionType;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import org.jetbrains.annotations.NotNull;

@InterfaceStability.Evolving
@ExtensionType("validation")
public interface ResourceValidation extends
        HasMetadataAcceptable,
        Extension,
        Configurable {

    /**
     * @return the name of this validation policy.
     */
    default String name() {
        final String className = this.getClass().getSimpleName();
        return className.replaceAll("Validation", "");
    }

    /**
     * Validates the given the resource.
     *
     * @param resource              the resource object to validate.
     * @throws ValidationException  if the given {@link HasMetadata} object is not valid.
     */
    void validate(@NotNull final HasMetadata resource) throws ValidationException;
}
