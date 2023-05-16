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
package io.streamthoughts.jikkou.api.control;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the metadata attached to a change task operation result.
 */
public final class ChangeMetadata {

    private final Throwable error;

    /**
     * Creates a new {@link ChangeMetadata}.
     */
    public ChangeMetadata() {
        this(null);
    }

    /**
     * Creates a new {@link ChangeMetadata}.
     *
     * @param error the error.
     */
    public ChangeMetadata(@Nullable Throwable error) {
        this.error = error;
    }

    public Optional<Throwable> getError() {
        return Optional.ofNullable(error);
    }
}
