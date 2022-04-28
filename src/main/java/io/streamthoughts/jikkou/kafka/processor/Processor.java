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
package io.streamthoughts.jikkou.kafka.processor;

import io.streamthoughts.jikkou.kafka.config.Configurable;
import io.streamthoughts.jikkou.kafka.model.V1SpecFile;
import io.streamthoughts.jikkou.kafka.transforms.Transformation;
import io.streamthoughts.jikkou.kafka.validations.Validation;
import io.vavr.Lazy;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @param <T>   the processor-type.
 */
public interface Processor<T extends Processor<T>> extends Configurable {

    /**
     * Creates a new {@link Processor} from this one and register the specified {@code transformation} into it.
     *
     * @param transformation  the {@link Transformation} to register.
     * @return  a new immutable {@link Processor}.
     */
    @NotNull T withTransformation(@NotNull final Lazy<Transformation> transformation);

    /**
     * Creates a new {@link Processor} from this one and register the specified {@code validation} into it.
     *
     * @param validation  the {@link Validation} to register .
     * @return  a new immutable {@link Processor}.
     */
    @NotNull T withValidation(@NotNull final Lazy<Validation> validation);

    /**
     * Applies this processor to the specified specification file object.
     *
     * @param file  the {@link V1SpecFile} to process.
     * @return      a new {@link V1SpecFile}.
     */
    @NotNull V1SpecFile apply(@NotNull final V1SpecFile file);
}
