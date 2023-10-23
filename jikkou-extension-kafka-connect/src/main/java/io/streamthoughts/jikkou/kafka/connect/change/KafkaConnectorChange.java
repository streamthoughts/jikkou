/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.connect.change;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.change.Change;
import io.streamthoughts.jikkou.core.change.ChangeType;
import io.streamthoughts.jikkou.core.change.ConfigEntryChange;
import io.streamthoughts.jikkou.core.change.ValueChange;
import io.streamthoughts.jikkou.kafka.connect.models.KafkaConnectorState;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A KafkaConnectorChange.
 *
 * @param name           the connector's name.
 * @param operation      the type of operation that changed the data.
 * @param connectorClass the connector class.
 * @param tasksMax       the maximum tasks for this connector.
 * @param config         the connector's configuration.
 */
@Reflectable
public record KafkaConnectorChange(@JsonProperty("operation") ChangeType operation,
                                   @JsonIgnore String name,
                                   @JsonProperty("connectorClass") ValueChange<String> connectorClass,
                                   @JsonProperty("tasksMax") ValueChange<Integer> tasksMax,
                                   @JsonProperty ("state") ValueChange<KafkaConnectorState> state,
                                   @JsonIgnore List<ConfigEntryChange> config
) implements Change {

    @JsonProperty("config")
    public Map<String, ConfigEntryChange> getConfigMap() {
        return config.stream().collect(Collectors.toMap(ConfigEntryChange::name, Function.identity(), (v1, v2) -> v1, TreeMap::new));
    }

    @JsonIgnore
    public boolean isStateOnlyChange() {
        if (operation != ChangeType.UPDATE)
            return false;

        if (connectorClass.operation() != ChangeType.NONE)
            return false;

        if (tasksMax.operation() != ChangeType.NONE)
            return false;

        if (Change.computeChangeTypeFrom(connectorClass) != ChangeType.NONE)
            return false;

        return state.operation() != ChangeType.NONE;
    }

}
