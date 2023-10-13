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

import io.streamthoughts.jikkou.api.change.ChangeResponse;
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.model.GenericResourceChange;
import io.streamthoughts.jikkou.common.utils.AsyncUtils;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi;
import io.streamthoughts.jikkou.kafka.connect.models.KafkaConnectorState;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class KafkaConnectorChangeHandlerTest {


    @Test
    void shouldUpdateConfigForAddChange() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, "test");

        KafkaConnectorChange change = new KafkaConnectorChange(
                ChangeType.ADD,
                "test",
                ValueChange.withAfterValue("???"),
                ValueChange.withAfterValue(1),
                ValueChange.withAfterValue(KafkaConnectorState.RUNNING),
                List.of()
        );

        List<ChangeResponse<KafkaConnectorChange>> results = handler.apply(List.of(new GenericResourceChange<>(change)));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.get(0).getResults());
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
               .createOrUpdateConnector(Mockito.eq("test"), Mockito.anyMap());
    }

    @Test
    void shouldDeleteConnectorForDeleteChange() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, "test");

        KafkaConnectorChange change = new KafkaConnectorChange(
                ChangeType.DELETE,
                "test",
                ValueChange.withBeforeValue("???"),
                ValueChange.withBeforeValue(1),
                ValueChange.withBeforeValue(KafkaConnectorState.RUNNING),
                List.of()
        );

        List<ChangeResponse<KafkaConnectorChange>> results = handler.apply(List.of(new GenericResourceChange<>(change)));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.get(0).getResults());
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
                .deleteConnector(Mockito.eq("test"));
    }

    @Test
    void shouldPauseConnectorForPausedStageOnlyChange() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, "test");

        KafkaConnectorChange change = new KafkaConnectorChange(
                ChangeType.UPDATE,
                "test",
                ValueChange.none("???"),
                ValueChange.none(1),
                ValueChange.withAfterValue(KafkaConnectorState.PAUSED),
                List.of()
        );

        List<ChangeResponse<KafkaConnectorChange>> results = handler.apply(List.of(new GenericResourceChange<>(change)));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.get(0).getResults());
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
                .pauseConnector(Mockito.eq("test"));
    }

    @Test
    void shouldResumeConnectorForRunningStateOnlyChange() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, "test");

        KafkaConnectorChange change = new KafkaConnectorChange(
                ChangeType.UPDATE,
                "test",
                ValueChange.none("???"),
                ValueChange.none(1),
                ValueChange.withAfterValue(KafkaConnectorState.RUNNING),
                List.of()
        );

        List<ChangeResponse<KafkaConnectorChange>> results = handler.apply(List.of(new GenericResourceChange<>(change)));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.get(0).getResults());
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
                .resumeConnector(Mockito.eq("test"));
    }

    @Test
    void shouldStopConnectorForStoppedStateOnlyChange() {
        KafkaConnectApi mkKafkaConnectApi = Mockito.mock(KafkaConnectApi.class);
        KafkaConnectorChangeHandler handler = new KafkaConnectorChangeHandler(mkKafkaConnectApi, "test");

        KafkaConnectorChange change = new KafkaConnectorChange(
                ChangeType.UPDATE,
                "test",
                ValueChange.none("???"),
                ValueChange.none(1),
                ValueChange.withAfterValue(KafkaConnectorState.STOPPED),
                List.of()
        );

        List<ChangeResponse<KafkaConnectorChange>> results = handler.apply(List.of(new GenericResourceChange<>(change)));
        Assertions.assertEquals(1, results.size());

        AsyncUtils.getValue(results.get(0).getResults());
        Mockito.verify(mkKafkaConnectApi, Mockito.times(1))
                .stopConnector(Mockito.eq("test"));
    }
}