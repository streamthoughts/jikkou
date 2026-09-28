/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.connect.change;

import io.jikkou.common.utils.AsyncUtils;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.ChangeMetadata;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.kafka.connect.api.KafkaConnectApi;
import io.jikkou.kafka.connect.api.data.ConnectorCreateRequest;
import io.jikkou.kafka.connect.models.KafkaConnectorState;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

class KafkaConnectorChangeHandlerTest {

    public static final String TEST_CONNECTOR_NAME = "test";

    @Test
    void shouldCreateConnectorWithNoInitialStateForRunningState() {
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

        List<ChangeResponse> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.getFirst().getResults());

        ArgumentCaptor<ConnectorCreateRequest> requestCaptor = ArgumentCaptor.forClass(ConnectorCreateRequest.class);
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
                .createConnector(requestCaptor.capture());

        ConnectorCreateRequest request = requestCaptor.getValue();
        Assertions.assertEquals(TEST_CONNECTOR_NAME, request.name());
        Assertions.assertNull(request.initialState());
    }

    @Test
    void shouldCreateConnectorWithStoppedInitialState() {
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
                        .withChange(StateChange.create("state", KafkaConnectorState.STOPPED))
                        .build()
                )
                .build();

        List<ChangeResponse> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.getFirst().getResults());

        ArgumentCaptor<ConnectorCreateRequest> requestCaptor = ArgumentCaptor.forClass(ConnectorCreateRequest.class);
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
                .createConnector(requestCaptor.capture());

        ConnectorCreateRequest request = requestCaptor.getValue();
        Assertions.assertEquals(TEST_CONNECTOR_NAME, request.name());
        Assertions.assertEquals("STOPPED", request.initialState());
    }

    @Test
    void shouldCreateConnectorWithPausedInitialState() {
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
                        .withChange(StateChange.create("state", KafkaConnectorState.PAUSED))
                        .build()
                )
                .build();

        List<ChangeResponse> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.getFirst().getResults());

        ArgumentCaptor<ConnectorCreateRequest> requestCaptor = ArgumentCaptor.forClass(ConnectorCreateRequest.class);
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
                .createConnector(requestCaptor.capture());

        ConnectorCreateRequest request = requestCaptor.getValue();
        Assertions.assertEquals(TEST_CONNECTOR_NAME, request.name());
        Assertions.assertEquals("PAUSED", request.initialState());
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

        List<ChangeResponse> results = handler.handleChanges(List.of(change));
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

        List<ChangeResponse> results = handler.handleChanges(List.of(change));
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

        List<ChangeResponse> results = handler.handleChanges(List.of(change));
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

        List<ChangeResponse> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.getFirst().getResults());
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
                .stopConnector(Mockito.eq(TEST_CONNECTOR_NAME));
    }

    @Test
    void shouldStopConnectorBeforeUpdatingConfigGivenConfigAndStoppedStateChange() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, TEST_CONNECTOR_NAME);

        ResourceChange change = newConfigAndStateChange(KafkaConnectorState.RUNNING, KafkaConnectorState.STOPPED);

        List<ChangeResponse> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());
        assertNoError(results.getFirst());

        InOrder inOrder = Mockito.inOrder(mkKafkaConnectApi);
        inOrder.verify(mkKafkaConnectApi).stopConnector(TEST_CONNECTOR_NAME);
        inOrder.verify(mkKafkaConnectApi).createOrUpdateConnector(
            Mockito.eq(TEST_CONNECTOR_NAME), Mockito.argThat(config -> "200".equals(config.get("flush.size"))));
    }

    @Test
    void shouldPauseConnectorBeforeUpdatingConfigGivenConfigAndPausedStateChange() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, TEST_CONNECTOR_NAME);

        ResourceChange change = newConfigAndStateChange(KafkaConnectorState.RUNNING, KafkaConnectorState.PAUSED);

        List<ChangeResponse> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());
        assertNoError(results.getFirst());

        InOrder inOrder = Mockito.inOrder(mkKafkaConnectApi);
        inOrder.verify(mkKafkaConnectApi).pauseConnector(TEST_CONNECTOR_NAME);
        inOrder.verify(mkKafkaConnectApi).createOrUpdateConnector(Mockito.eq(TEST_CONNECTOR_NAME), Mockito.anyMap());
    }

    @Test
    void shouldResumeConnectorAfterUpdatingConfigGivenConfigAndRunningStateChange() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, TEST_CONNECTOR_NAME);

        ResourceChange change = newConfigAndStateChange(KafkaConnectorState.STOPPED, KafkaConnectorState.RUNNING);

        List<ChangeResponse> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());
        assertNoError(results.getFirst());

        InOrder inOrder = Mockito.inOrder(mkKafkaConnectApi);
        inOrder.verify(mkKafkaConnectApi).createOrUpdateConnector(Mockito.eq(TEST_CONNECTOR_NAME), Mockito.anyMap());
        inOrder.verify(mkKafkaConnectApi).resumeConnector(TEST_CONNECTOR_NAME);
    }

    @Test
    void shouldReportErrorAndSkipConfigUpdateGivenStateTransitionFails() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        Mockito.doThrow(new RuntimeException("pause failed"))
            .when(mkKafkaConnectApi).pauseConnector(TEST_CONNECTOR_NAME);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, TEST_CONNECTOR_NAME);

        ResourceChange change = newConfigAndStateChange(KafkaConnectorState.RUNNING, KafkaConnectorState.PAUSED);

        List<ChangeResponse> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());

        List<ChangeMetadata> metadata = results.getFirst().getResults().join();
        Assertions.assertTrue(metadata.stream().anyMatch(m -> m.getError().isPresent()));
        Mockito.verify(mkKafkaConnectApi, Mockito.never())
            .createOrUpdateConnector(Mockito.anyString(), Mockito.anyMap());
    }

    @Test
    void shouldReportErrorGivenNonActionableTargetState() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, TEST_CONNECTOR_NAME);

        ResourceChange change = GenericResourceChange
                .builder()
                .withMetadata(ObjectMeta.builder().withName(TEST_CONNECTOR_NAME).build())
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.UPDATE)
                        .withChange(StateChange.none("connectorClass", "???"))
                        .withChange(StateChange.none("tasksMax", 1))
                        .withChange(StateChange.update("state", KafkaConnectorState.RUNNING, KafkaConnectorState.FAILED))
                        .build()
                )
                .build();

        List<ChangeResponse> results = handler.handleChanges(List.of(change));
        Assertions.assertEquals(1, results.size());

        List<ChangeMetadata> metadata = results.getFirst().getResults().join();
        Assertions.assertTrue(metadata.stream().anyMatch(m -> m.getError().isPresent()));
        Mockito.verifyNoInteractions(mkKafkaConnectApi);
    }

    private static ResourceChange newConfigAndStateChange(KafkaConnectorState before, KafkaConnectorState after) {
        return GenericResourceChange
                .builder()
                .withMetadata(ObjectMeta.builder().withName(TEST_CONNECTOR_NAME).build())
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.UPDATE)
                        .withChange(StateChange.none("connectorClass", "???"))
                        .withChange(StateChange.none("tasksMax", 1))
                        .withChange(StateChange.update("state", before, after))
                        .withChange(StateChange.update("config.flush.size", "100", "200"))
                        .build()
                )
                .build();
    }

    private static void assertNoError(ChangeResponse response) {
        List<ChangeMetadata> metadata = response.getResults().join();
        Assertions.assertTrue(metadata.stream().allMatch(m -> m.getError().isEmpty()), metadata::toString);
    }
}
