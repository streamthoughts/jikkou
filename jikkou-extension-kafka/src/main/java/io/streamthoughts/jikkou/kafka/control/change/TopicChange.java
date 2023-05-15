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
package io.streamthoughts.jikkou.kafka.control.change;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.streamthoughts.jikkou.api.control.Change;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ConfigEntryChange;
import io.streamthoughts.jikkou.api.control.ValueChange;
import io.streamthoughts.jikkou.api.model.Nameable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@AllArgsConstructor
public final class TopicChange implements Change, Nameable {

    private final String name;
    private final ChangeType operation;
    private final ValueChange<Integer> partitions;
    private final ValueChange<Short> replicas;
    private final List<ConfigEntryChange> configs;

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public ChangeType getChangeType() {
        return operation;
    }

    public ValueChange<Integer> getPartitions() {
        return partitions;
    }

    public ValueChange<Short> getReplicas() {
        return replicas;
    }

    public Map<String, ConfigEntryChange> getConfigs() {
        return  getConfigEntryChanges()
                .stream()
                .collect(Collectors.toMap(ConfigEntryChange::getName, it -> it));
    }

    @JsonIgnore
    public List<ConfigEntryChange> getConfigEntryChanges() {
        return Optional.ofNullable(configs).orElse(Collections.emptyList());
    }

    public boolean hasConfigEntryChanges() {
        return  getConfigEntryChanges()
                .stream()
                .anyMatch(it -> !it.getChangeType().equals(ChangeType.NONE));
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicChange change = (TopicChange) o;
        return operation == change.operation &&
            Objects.equals(name, change.name) &&
            Objects.equals(partitions, change.partitions) &&
            Objects.equals(replicas, change.replicas) &&
            Objects.equals(configs, change.configs);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(name, operation, partitions, replicas, configs);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "TopicChange{" +
                "name='" + name + '\'' +
                ", operation=" + operation +
                ", partitions=" + partitions +
                ", replicas=" + replicas +
                ", configs=" + configs +
                '}';
    }
}
