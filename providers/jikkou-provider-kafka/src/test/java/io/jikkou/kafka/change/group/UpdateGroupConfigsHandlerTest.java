/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.change.group;

import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.kafka.models.V1KafkaConsumerGroup;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.AlterConfigsResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.ConfigResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class UpdateGroupConfigsHandlerTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldAlterGroupConfigsWithGroupResourceType() {
        AdminClient client = Mockito.mock(AdminClient.class);
        AlterConfigsResult result = Mockito.mock(AlterConfigsResult.class);
        Mockito.when(result.values()).thenReturn(Map.of(
            new ConfigResource(ConfigResource.Type.GROUP, "g1"), KafkaFuture.completedFuture(null)));
        ArgumentCaptor<Map<ConfigResource, Collection<AlterConfigOp>>> captor =
            ArgumentCaptor.forClass(Map.class);
        Mockito.when(client.incrementalAlterConfigs(captor.capture())).thenReturn(result);

        ResourceChange change = GenericResourceChange.builder(V1KafkaConsumerGroup.class)
            .withMetadata(ObjectMeta.builder().withName("g1").build())
            .withSpec(ResourceChangeSpec.builder()
                .withOperation(Operation.UPDATE)
                .withChanges(List.of(StateChange.builder()
                    .withName("config.share.auto.offset.reset")
                    .withOp(Operation.CREATE)
                    .withAfter("earliest")
                    .build()))
                .build())
            .build();

        UpdateGroupConfigsHandler handler = new UpdateGroupConfigsHandler(client);
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));

        Assertions.assertEquals(1, responses.size());
        ConfigResource resource = captor.getValue().keySet().iterator().next();
        Assertions.assertEquals(ConfigResource.Type.GROUP, resource.type());
        Assertions.assertEquals("g1", resource.name());
        AlterConfigOp op = captor.getValue().values().iterator().next().iterator().next();
        Assertions.assertEquals(AlterConfigOp.OpType.SET, op.opType());
        Assertions.assertEquals("share.auto.offset.reset", op.configEntry().name());
    }
}
