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
package io.streamthoughts.jikkou.api.control;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.streamthoughts.jikkou.api.model.Nameable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ConfigEntryChange implements Change<String>, Nameable {

    private final ValueChange<Object> valueChange;
    private final String name;

    /**
     * Creates a new {@link ConfigEntryChange} instance.
     *
     * @param name          the config-entry name.
     * @param valueChange   the {@link ValueChange}.
     */
    public ConfigEntryChange(@NotNull final String name,
                             @NotNull final ValueChange<Object> valueChange) {
        this.name = Objects.requireNonNull(name, "'name' should not be null");
        this.valueChange = Objects.requireNonNull(valueChange, "'valueChange' should not be null");
    }

    /** {@inheritDoc} */
    @Override
    @JsonIgnore
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    @JsonProperty("operation")
    public ChangeType getChange() {
        return valueChange.type();
    }

    /** {@inheritDoc} */
    @JsonUnwrapped
    public ValueChange<Object> getValueChange() {
        return valueChange;
    }

    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigEntryChange that = (ConfigEntryChange) o;
        return Objects.equals(valueChange, that.valueChange) && Objects.equals(name, that.name);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(valueChange, name);
    }
}
