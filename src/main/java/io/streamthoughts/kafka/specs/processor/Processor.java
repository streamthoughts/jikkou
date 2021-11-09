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
package io.streamthoughts.kafka.specs.processor;

import io.streamthoughts.kafka.specs.config.Configurable;
import io.streamthoughts.kafka.specs.model.V1SpecFile;
import io.streamthoughts.kafka.specs.transforms.Transformation;
import io.streamthoughts.kafka.specs.validations.Validation;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @param <T>   the processor-type.
 */
public interface Processor<T extends Processor<T>> extends Configurable {

    @NotNull T withTransformation(@NotNull final Transformation transformation);

    @NotNull T withValidation(@NotNull final Validation validation);

    @NotNull V1SpecFile apply(@NotNull final V1SpecFile spec);
}
