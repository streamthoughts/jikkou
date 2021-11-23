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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.streamthoughts.kafka.specs.resources.Named;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TopicChange implements Change<String>, Named {

    private final String name;
    private final OperationType operation;

    private final Option<ValueChange<Integer>> partitions;
    private final Option<ValueChange<Short>> replicationFactor;

    private final List<ConfigEntryChange> configs;

    public TopicChange(@NotNull final String name,
                       @NotNull final OperationType operation,
                       @Nullable final ValueChange<Integer> partitionChange,
                       @Nullable final ValueChange<Short> replicationFactorChange,
                       @NotNull final List<ConfigEntryChange> configs) {
        this.name = Objects.requireNonNull(name, "'name should not be nul'");
        this.operation = Objects.requireNonNull(operation, "'operation should not be nul'");
        this.partitions = Option.of(partitionChange);
        this.replicationFactor = Option.of(replicationFactorChange);
        this.configs = configs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationType getOperation() {
        return operation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return name;
    }

    @JsonProperty
    public Option<ValueChange<Integer>> getPartitions() {
        return partitions;
    }

    @JsonProperty
    public Option<ValueChange<Short>> getReplicationFactor() {
        return replicationFactor;
    }

    @JsonIgnore
    public List<ConfigEntryChange> getConfigEntryChanges() {
        return configs;
    }

    public Map<String, ValueChange<Object>> getConfigs() {
        return configs.stream().collect(Collectors.toMap(ConfigEntryChange::name, it -> it));
    }

    public boolean hasConfigEntryChanges() {
        return configs.stream().anyMatch(it -> !it.getOperation().equals(OperationType.NONE));
    }

    public static class Builder {
        private String name;
        private OperationType operation;
        private ValueChange<Integer> partitionsChange;
        private ValueChange<Short> replicationFactorChange;
        private List<ConfigEntryChange> configs = new LinkedList<>();

        public Builder setName(final String name) {
            this.name = name;
            return this;
        }

        public Builder setOperation(final OperationType operation) {
            this.operation = operation;
            return this;
        }

        public Builder setPartitionsChange(final ValueChange<Integer> partitionChange) {
            this.partitionsChange = partitionChange;
            return this;
        }

        public Builder setReplicationFactorChange(final ValueChange<Short> replicationFactorChange) {
            this.replicationFactorChange = replicationFactorChange;
            return this;
        }

        public Builder setConfigs(final List<ConfigEntryChange> configs) {
            this.configs = configs;
            return this;
        }

        public Builder addConfigChange(final ConfigEntryChange change) {
            this.configs.add(change);
            return this;
        }

        public TopicChange build() {
            return new TopicChange(name, operation, partitionsChange, replicationFactorChange, configs);
        }
    }

}
