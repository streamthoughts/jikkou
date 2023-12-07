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

import io.streamthoughts.jikkou.common.utils.AsyncUtils;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi;
import io.streamthoughts.jikkou.kafka.connect.models.KafkaConnectorState;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class KafkaConnectorChangeHandlerTest {

    public static final String TEST_CONNECTOR_NAME = "test";

    @Test
    void shouldUpdateConfigForAddChange() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, TEST_CONNECTOR_NAME);

        ResourceChange change = GenericResourceChange
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_CONNECTOR_NAME)
                        .build()
                )
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.CREATE)
                        .withChange(StateChange.create("connectorClass", "???"))
                        .withChange(StateChange.create("tasksMax", 1))
                        .withChange(StateChange.create("state", KafkaConnectorState.RUNNING))
                        .build()
                )
                .build();

        List<ChangeResponse<ResourceChange>> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.getFirst().getResults());
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
                .createOrUpdateConnector(Mockito.eq(TEST_CONNECTOR_NAME), Mockito.anyMap());
    }

    @Test
    void shouldDeleteConnectorForDeleteChange() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, TEST_CONNECTOR_NAME);

        ResourceChange change = GenericResourceChange
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_CONNECTOR_NAME)
                        .build()
                )
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.DELETE)
                        .withChange(StateChange.delete("connectorClass", "???"))
                        .withChange(StateChange.delete("tasksMax", 1))
                        .withChange(StateChange.delete("state", KafkaConnectorState.RUNNING))
                        .build()
                )
                .build();

        List<ChangeResponse<ResourceChange>> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.getFirst().getResults());
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
                .deleteConnector(Mockito.eq(TEST_CONNECTOR_NAME));
    }

    @Test
    void shouldPauseConnectorForPausedStageOnlyChange() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, TEST_CONNECTOR_NAME);

        ResourceChange change = GenericResourceChange
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_CONNECTOR_NAME)
                        .build()
                )
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.UPDATE)
                        .withChange(StateChange.none("connectorClass", "???"))
                        .withChange(StateChange.none("tasksMax", 1))
                        .withChange(StateChange.with("state", null, KafkaConnectorState.PAUSED))
                        .build()
                )
                .build();

        List<ChangeResponse<ResourceChange>> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.getFirst().getResults());
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
                .pauseConnector(Mockito.eq(TEST_CONNECTOR_NAME));
    }

    @Test
    void shouldResumeConnectorForRunningStateOnlyChange() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, TEST_CONNECTOR_NAME);

        ResourceChange change = GenericResourceChange
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_CONNECTOR_NAME)
                        .build()
                )
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.UPDATE)
                        .withChange(StateChange.none("connectorClass", "???"))
                        .withChange(StateChange.none("tasksMax", 1))
                        .withChange(StateChange.create("state", KafkaConnectorState.RUNNING))
                        .build()
                )
                .build();

        List<ChangeResponse<ResourceChange>> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.getFirst().getResults());
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
                .resumeConnector(Mockito.eq(TEST_CONNECTOR_NAME));
    }

    @Test
    void shouldStopConnectorForStoppedStateOnlyChange() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, TEST_CONNECTOR_NAME);

        ResourceChange change = GenericResourceChange
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_CONNECTOR_NAME)
                        .build()
                )
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.UPDATE)
                        .withChange(StateChange.none("connectorClass", "???"))
                        .withChange(StateChange.none("tasksMax", 1))
                        .withChange(StateChange.create("state", KafkaConnectorState.STOPPED))
                        .build()
                )
                .build();

        List<ChangeResponse<ResourceChange>> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.getFirst().getResults());
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
                .stopConnector(Mockito.eq(TEST_CONNECTOR_NAME));
    }
}