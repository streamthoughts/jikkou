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
package io.streamthoughts.kafka.specs.change;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.streamthoughts.kafka.specs.resources.Named;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConfigEntryChange extends ValueChange<String> implements Change<ConfigEntryChange>, Named {

    private final String name;

    /**
     * Creates a new {@link ConfigEntryChange} instance.
     *
     * @param name          the config-entry name.
     */
    public ConfigEntryChange(@NotNull final String name,
                             @NotNull final ValueChange<String> valueChange) {
        super(valueChange);
        this.name = Objects.requireNonNull(name, "'name' should not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public String name() {
        return name;
    }

}
