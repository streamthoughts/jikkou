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
package io.streamthoughts.kafka.specs;

import io.streamthoughts.kafka.specs.model.V1SpecFile;
import io.streamthoughts.kafka.specs.model.V1SpecsObject;
import io.streamthoughts.kafka.specs.transforms.Transformation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SpecFileValidator {

    private final List<Transformation> transformations;

    /**
     * Creates a new {@link SpecFileValidator} instance.
     */
    public SpecFileValidator() {
        this(new LinkedList<>());
    }

    /**
     * Creates a new {@link SpecFileValidator} instance.
     *
     * @param transformations   the list of {@link Transformation}.
     */
    public SpecFileValidator(final @NotNull List<Transformation> transformations) {
        this.transformations = Objects.requireNonNull(transformations, "'transformations' cannot be null");
    }

    public @NotNull SpecFileValidator withTransforms(@NotNull final Transformation transformation) {
        this.transformations.add(transformation);
        return this;
    }

    public @NotNull V1SpecFile apply(@NotNull final V1SpecFile file) {
        V1SpecsObject specs = file.specs();
        for (Transformation transformation : transformations) {
            specs = transformation.transform(specs);
        }
        return new V1SpecFile(file.metadata(), specs);
    }
}
