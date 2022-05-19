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
package io.streamthoughts.jikkou.api.transforms;

import io.streamthoughts.jikkou.api.config.Configurable;
import io.streamthoughts.jikkou.api.model.V1SpecFile;
import io.streamthoughts.jikkou.api.model.V1SpecObject;
import io.streamthoughts.jikkou.api.extensions.Extension;
import org.jetbrains.annotations.NotNull;

/**
 * Transform an input {@link V1SpecObject}.
 */
@FunctionalInterface
public interface Transformation extends Extension, Configurable {

    /**
     * Applies the transformation on the given {@link V1SpecFile} object.
     *
     * @param V1SpecsObject    the {@link V1SpecObject} object to transform.
     * @return                 a {@link V1SpecObject}.
     */
    @NotNull V1SpecObject transform(@NotNull final V1SpecObject V1SpecsObject);

}
