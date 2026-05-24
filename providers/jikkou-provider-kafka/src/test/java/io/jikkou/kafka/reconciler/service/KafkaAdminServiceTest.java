/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.reconciler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jikkou.core.exceptions.JikkouRuntimeException;
import io.jikkou.kafka.collections.V1KafkaConsumerGroupList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

class KafkaAdminServiceTest {

    private AdminClient adminClient;
    private KafkaAdminService service;

    @BeforeEach
    void setUp() {
        adminClient = mock(AdminClient.class);
        service = new KafkaAdminService(adminClient);
    }

    @Test
    void shouldReturnEmptyListWhenNoConsumerGroups() {
        // Given
        DescribeConsumerGroupsResult result = mock(DescribeConsumerGroupsResult.class);
        when(adminClient.describeConsumerGroups(List.of())).thenReturn(result);
        when(result.describedGroups()).thenReturn(Map.of());

        // When
        V1KafkaConsumerGroupList groups = service.listConsumerGroups(List.of(), false);

        // Then
        assertNotNull(groups);
        assertTrue(groups.getItems().isEmpty());
    }

    @Test
    void shouldReturnEmptyMonoWhenNoConsumerGroupsAsync() {
        // Given
        DescribeConsumerGroupsResult result = mock(DescribeConsumerGroupsResult.class);
        when(adminClient.describeConsumerGroups(List.of())).thenReturn(result);
        when(result.describedGroups()).thenReturn(Map.of());

        // When
        Mono<V1KafkaConsumerGroupList> mono = service.listConsumerGroupsAsync(List.of(), false);

        // Then
        V1KafkaConsumerGroupList groups = mono.block();
        assertNotNull(groups);
        assertTrue(groups.getItems().isEmpty());
    }

    @Test
    void shouldChainCauseWhenAdminClientThrows() {
        // Given
        RuntimeException cause = new RuntimeException("kafka unreachable");
        when(adminClient.describeConsumerGroups(List.of("g1"))).thenThrow(cause);

        // When
        JikkouRuntimeException ex = assertThrows(JikkouRuntimeException.class,
            () -> service.listConsumerGroups(List.of("g1"), false));

        // Then
        assertNotNull(ex.getCause(), "JikkouRuntimeException must chain the original cause");
        assertSame(cause, ex.getCause(),
            "The chained cause must be the original RuntimeException thrown by the AdminClient");
    }

    @Test
    void shouldChainCauseWhenAdminClientThrowsAsync() {
        // Given
        RuntimeException cause = new RuntimeException("kafka unreachable");
        when(adminClient.describeConsumerGroups(List.of("g1"))).thenThrow(cause);

        // When
        Mono<V1KafkaConsumerGroupList> mono = service.listConsumerGroupsAsync(List.of("g1"), false);

        // Then
        JikkouRuntimeException ex = assertThrows(JikkouRuntimeException.class, mono::block);
        assertNotNull(ex.getCause(), "JikkouRuntimeException must chain the original cause");
        assertSame(cause, ex.getCause(),
            "The chained cause must be the original RuntimeException thrown by the AdminClient");
    }

    @Test
    void shouldNotDoubleWrapJikkouRuntimeException() {
        // Given - AdminClient itself throws a JikkouRuntimeException (e.g. from a wrapping layer)
        JikkouRuntimeException original = new JikkouRuntimeException("already wrapped");
        when(adminClient.describeConsumerGroups(List.of("g1"))).thenThrow(original);

        // When
        JikkouRuntimeException ex = assertThrows(JikkouRuntimeException.class,
            () -> service.listConsumerGroups(List.of("g1"), false));

        // Then
        assertSame(original, ex,
            "An existing JikkouRuntimeException must be propagated as-is, not re-wrapped");
    }

    @Test
    void shouldReturnSameResultFromSyncAndAsyncVariants() {
        // Given
        DescribeConsumerGroupsResult result = mock(DescribeConsumerGroupsResult.class);
        when(adminClient.describeConsumerGroups(List.of())).thenReturn(result);
        when(result.describedGroups()).thenReturn(Map.of());

        // When
        V1KafkaConsumerGroupList sync = service.listConsumerGroups(List.of(), false);
        V1KafkaConsumerGroupList async = service.listConsumerGroupsAsync(List.of(), false).block();

        // Then
        assertEquals(sync.getItems(), async.getItems());
    }

    @Test
    void shouldExpandBareTopicsViaDescribeTopics() throws Exception {
        AdminClient admin = mock(AdminClient.class);
        DescribeTopicsResult result = mock(DescribeTopicsResult.class);

        Node node = new Node(0, "localhost", 9092);
        TopicDescription description = new TopicDescription(
            "my-topic",
            false,
            List.of(
                new TopicPartitionInfo(0, node, List.of(node), List.of(node)),
                new TopicPartitionInfo(1, node, List.of(node), List.of(node))
            )
        );
        Map<String, TopicDescription> byName = Map.of("my-topic", description);
        when(result.allTopicNames()).thenReturn(KafkaFuture.completedFuture(byName));
        when(admin.describeTopics(anyCollection())).thenReturn(result);

        KafkaAdminService service = new KafkaAdminService(admin);

        List<TopicPartition> resolved = service.resolveTopicPartitions(
            List.of(new TopicPartitionSelector("my-topic", OptionalInt.empty()))
        ).get();

        assertEquals(
            Set.of(new TopicPartition("my-topic", 0), new TopicPartition("my-topic", 1)),
            Set.copyOf(resolved)
        );
        verify(admin, times(1)).describeTopics(anyCollection());
    }

    @Test
    void shouldUseSpecificPartitionsDirectlyWithoutDescribingTopics() throws Exception {
        AdminClient admin = mock(AdminClient.class);
        KafkaAdminService service = new KafkaAdminService(admin);

        List<TopicPartition> resolved = service.resolveTopicPartitions(
            List.of(
                new TopicPartitionSelector("other-topic", OptionalInt.of(5)),
                new TopicPartitionSelector("other-topic", OptionalInt.of(7))
            )
        ).get();

        assertEquals(
            Set.of(new TopicPartition("other-topic", 5), new TopicPartition("other-topic", 7)),
            Set.copyOf(resolved)
        );
        verify(admin, never()).describeTopics(anyCollection());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUnionBareAndSpecificEntriesAndDedupe() throws Exception {
        AdminClient admin = mock(AdminClient.class);
        DescribeTopicsResult result = mock(DescribeTopicsResult.class);

        Node node = new Node(0, "localhost", 9092);
        TopicDescription bareDescription = new TopicDescription(
            "bare-topic",
            false,
            List.of(
                new TopicPartitionInfo(0, node, List.of(node), List.of(node)),
                new TopicPartitionInfo(1, node, List.of(node), List.of(node))
            )
        );
        when(result.allTopicNames()).thenReturn(
            KafkaFuture.completedFuture(Map.of("bare-topic", bareDescription))
        );
        when(admin.describeTopics(anyCollection())).thenReturn(result);

        KafkaAdminService service = new KafkaAdminService(admin);

        // bare-topic expands to (bare-topic, 0), (bare-topic, 1)
        // typed entry adds (bare-topic, 1) — already present, must dedupe to one
        // typed entry adds (other-topic, 3)
        List<TopicPartition> resolved = service.resolveTopicPartitions(
            List.of(
                new TopicPartitionSelector("bare-topic", OptionalInt.empty()),
                new TopicPartitionSelector("bare-topic", OptionalInt.of(1)),
                new TopicPartitionSelector("other-topic", OptionalInt.of(3))
            )
        ).get();

        assertEquals(
            Set.of(
                new TopicPartition("bare-topic", 0),
                new TopicPartition("bare-topic", 1),
                new TopicPartition("other-topic", 3)
            ),
            Set.copyOf(resolved)
        );

        // exactly one describeTopics call, only for the bare topic
        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(admin, times(1)).describeTopics(captor.capture());
        assertEquals(List.of("bare-topic"), List.copyOf(captor.getValue()));
    }
}
