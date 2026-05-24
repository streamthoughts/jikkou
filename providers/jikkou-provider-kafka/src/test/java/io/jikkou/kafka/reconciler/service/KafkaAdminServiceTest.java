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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jikkou.core.exceptions.JikkouRuntimeException;
import io.jikkou.kafka.collections.V1KafkaConsumerGroupList;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
}
