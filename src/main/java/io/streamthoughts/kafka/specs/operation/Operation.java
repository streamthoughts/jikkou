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
package io.streamthoughts.kafka.specs.operation;

import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.change.Change;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface Operation<K, T extends Change<K>> extends Predicate<T>{

    /**
     * Checks whether the given change is supported by this operation.
     * @return {@code true} if the change can be accepted by this operation, {@code false} otherwise.
     */
    @Override
    boolean test(final T change);

    /**
     * Returns a textual description for the given {@link Change}.
     *
     * @param change    the {@link Change}.
     * @return          the textual description for this change.
     */
    Description getDescriptionFor(@NotNull final T change);

}
