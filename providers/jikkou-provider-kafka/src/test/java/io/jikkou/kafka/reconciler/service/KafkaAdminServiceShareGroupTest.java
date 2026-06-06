/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.reconciler.service;

import io.jikkou.kafka.models.V1KafkaShareGroup;
import io.jikkou.kafka.models.V1KafkaShareGroupMember;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ShareGroupDescription;
import org.apache.kafka.clients.admin.ShareMemberAssignment;
import org.apache.kafka.clients.admin.ShareMemberDescription;
import org.apache.kafka.common.GroupState;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class KafkaAdminServiceShareGroupTest {

    @Test
    void shouldMapShareGroupDescriptionToResource() {
        ShareMemberDescription member = new ShareMemberDescription(
            "member-1",
            Optional.of("rack-1"),
            "client-1",
            "host-1",
            new ShareMemberAssignment(Set.of(new TopicPartition("orders", 0))),
            5);
        ShareGroupDescription description = new ShareGroupDescription(
            "orders-queue",
            List.of(member),
            GroupState.STABLE,
            new Node(1, "broker-1", 9092),
            1,
            1);

        KafkaAdminService service = new KafkaAdminService(Mockito.mock(AdminClient.class));
        V1KafkaShareGroup group = service.mapToResource(description);

        Assertions.assertEquals("orders-queue", group.getMetadata().getName());
        Assertions.assertEquals("STABLE", group.getStatus().getState());
        List<V1KafkaShareGroupMember> members = group.getStatus().getMembers();
        Assertions.assertEquals(1, members.size());
        Assertions.assertEquals("member-1", members.get(0).getMemberId());
        Assertions.assertEquals("rack-1", members.get(0).getRackId());
        Assertions.assertEquals(List.of("orders-0"), members.get(0).getAssignments());
    }
}
