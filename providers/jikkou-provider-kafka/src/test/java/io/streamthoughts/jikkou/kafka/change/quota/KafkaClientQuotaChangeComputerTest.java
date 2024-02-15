/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.quota;

import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaConfigs;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaEntity;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaType;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaSpec;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaClientQuotaChangeComputerTest {

    public static final List<V1KafkaClientQuota> EMPTY_LIST = Collections.emptyList();

    @Test
    void shouldReturnAddChangeForNewQuota() {
        // Given
        KafkaClientQuotaChangeComputer computer = new KafkaClientQuotaChangeComputer(true);

        V1KafkaClientQuota resource = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .withConfigs(KafkaClientQuotaConfigs
                                .builder()
                                .withConsumerByteRate(OptionalDouble.of(1.0))
                                .withProducerByteRate(OptionalDouble.of(1.0))
                                .withRequestPercentage(OptionalDouble.of(1.0))
                                .build()
                        )
                        .build()
                )
                .build();

        // When
        List<ResourceChange> actual = computer.computeChanges(EMPTY_LIST, List.of(resource));

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange
                        .builder(V1KafkaClientQuota.class)
                        .withMetadata(resource.getMetadata())
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(Operation.CREATE)
                                .withData(resource.getSpec().getType().toEntities(resource.getSpec().getEntity()))
                                .withChanges(
                                        List.of(
                                                StateChange.create("request_percentage", 1.0),
                                                StateChange.create("producer_byte_rate", 1.0),
                                                StateChange.create("consumer_byte_rate", 1.0)
                                        )
                                )
                                .build()
                        )
                        .build()
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldReturnNoneChangeForEqualsQuota() {
        // Given
        KafkaClientQuotaChangeComputer computer = new KafkaClientQuotaChangeComputer(true);

        V1KafkaClientQuota resource = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .withConfigs(KafkaClientQuotaConfigs
                                .builder()
                                .withConsumerByteRate(OptionalDouble.of(1.0))
                                .withProducerByteRate(OptionalDouble.of(1.0))
                                .withRequestPercentage(OptionalDouble.of(1.0))
                                .build()
                        )
                        .build()
                )
                .build();

        // When
        List<ResourceChange> actual = computer.computeChanges(List.of(resource), List.of(resource));

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange
                        .builder(V1KafkaClientQuota.class)
                        .withMetadata(resource.getMetadata())
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(Operation.NONE)
                                .withData(resource.getSpec().getType().toEntities(resource.getSpec().getEntity()))
                                .withChanges(
                                        List.of(
                                                StateChange.none("request_percentage", 1.0),
                                                StateChange.none("producer_byte_rate", 1.0),
                                                StateChange.none("consumer_byte_rate", 1.0)
                                        )
                                )
                                .build()
                        )
                        .build()
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldReturnDeleteChangeForExistingQuotaDeleteTrue() {
        // Given
        KafkaClientQuotaChangeComputer computer = new KafkaClientQuotaChangeComputer(true);

        V1KafkaClientQuota resource = V1KafkaClientQuota.builder()
                .withMetadata(ObjectMeta.builder()
                        .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, true)
                        .build()
                )
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .withConfigs(KafkaClientQuotaConfigs
                                .builder()
                                .withConsumerByteRate(OptionalDouble.of(1.0))
                                .withProducerByteRate(OptionalDouble.of(1.0))
                                .withRequestPercentage(OptionalDouble.of(1.0))
                                .build()
                        )
                        .build()
                )
                .build();

        // When
        List<ResourceChange> actual = computer.computeChanges(List.of(resource), List.of(resource));

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange
                        .builder(V1KafkaClientQuota.class)
                        .withMetadata(resource.getMetadata())
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(Operation.DELETE)
                                .withData(resource.getSpec().getType().toEntities(resource.getSpec().getEntity()))
                                .withChanges(
                                        List.of(
                                                StateChange.delete("request_percentage", 1.0),
                                                StateChange.delete("producer_byte_rate", 1.0),
                                                StateChange.delete("consumer_byte_rate", 1.0)
                                        )
                                )
                                .build()
                        )
                        .build()
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldReturnNoDeleteChangeForNotExistingQuotaDeleteTrue() {
        // Given
        KafkaClientQuotaChangeComputer computer = new KafkaClientQuotaChangeComputer(true);

        V1KafkaClientQuota resource = V1KafkaClientQuota.builder()
                .withMetadata(ObjectMeta.builder()
                        .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, true)
                        .build()
                )
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .withConfigs(KafkaClientQuotaConfigs
                                .builder()
                                .withConsumerByteRate(OptionalDouble.of(1.0))
                                .withProducerByteRate(OptionalDouble.of(1.0))
                                .withRequestPercentage(OptionalDouble.of(1.0))
                                .build()
                        )
                        .build()
                )
                .build();

        // When
        List<ResourceChange> actual = computer.computeChanges(EMPTY_LIST, List.of(resource));

        // Then
        Assertions.assertEquals(Collections.emptyList(), actual);
    }

    @Test
    void shouldReturnUpdateChangeForExistingQuota() {
        // Given
        KafkaClientQuotaChangeComputer computer = new KafkaClientQuotaChangeComputer(true);

        V1KafkaClientQuota before = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .withConfigs(KafkaClientQuotaConfigs
                                .builder()
                                .withConsumerByteRate(OptionalDouble.of(1.0))
                                .build()
                        )
                        .build()
                )
                .build();

        V1KafkaClientQuota after = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .withConfigs(KafkaClientQuotaConfigs
                                .builder()
                                .withProducerByteRate(OptionalDouble.of(1.0))
                                .build()
                        )
                        .build()
                )
                .build();

        // When
        List<ResourceChange> actual = computer.computeChanges(List.of(before), List.of(after));

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange
                        .builder(V1KafkaClientQuota.class)
                        .withMetadata(after.getMetadata())
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(Operation.UPDATE)
                                .withData(before.getSpec().getType().toEntities(before.getSpec().getEntity()))
                                .withChanges(
                                        List.of(
                                                StateChange.create("producer_byte_rate", 1.0),
                                                StateChange.delete("consumer_byte_rate", 1.0)
                                        )
                                )
                                .build()
                        )
                        .build()
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldReturnUpdateChangeWithDeleteLimitForDeleteEnableTrue() {
        // Given
        KafkaClientQuotaChangeComputer computer = new KafkaClientQuotaChangeComputer(true);

        V1KafkaClientQuota before = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .withConfigs(KafkaClientQuotaConfigs
                                .builder()
                                .withConsumerByteRate(OptionalDouble.of(1.0))
                                .withProducerByteRate(OptionalDouble.of(1.0))
                                .build()
                        )
                        .build()
                )
                .build();

        V1KafkaClientQuota after = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .withConfigs(KafkaClientQuotaConfigs
                                .builder()
                                .withConsumerByteRate(OptionalDouble.of(1.0))
                                .build()
                        )
                        .build()
                )
                .build();

        // When
        List<ResourceChange> actual = computer.computeChanges(List.of(before), List.of(after));

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange
                        .builder(V1KafkaClientQuota.class)
                        .withMetadata(after.getMetadata())
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(Operation.UPDATE)
                                .withData(before.getSpec().getType().toEntities(before.getSpec().getEntity()))
                                .withChanges(
                                        List.of(
                                                StateChange.delete("producer_byte_rate", 1.0),
                                                StateChange.none("consumer_byte_rate", 1.0)
                                        )
                                )
                                .build()
                        )
                        .build()
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldReturnNoneChangeWithNoDeleteLimitForDeleteEnableFalse() {
        // Given
        KafkaClientQuotaChangeComputer computer = new KafkaClientQuotaChangeComputer(false);

        V1KafkaClientQuota before = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .withConfigs(KafkaClientQuotaConfigs
                                .builder()
                                .withProducerByteRate(OptionalDouble.of(0.0))
                                .build()
                        )
                        .build()
                )
                .build();

        V1KafkaClientQuota after = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .withConfigs(KafkaClientQuotaConfigs
                                .builder()
                                .build()
                        )
                        .build()
                )
                .build();

        // When
        List<ResourceChange> actual = computer.computeChanges(List.of(before), List.of(after));

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange
                        .builder(V1KafkaClientQuota.class)
                        .withMetadata(after.getMetadata())
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(Operation.NONE)
                                .withData(before.getSpec().getType().toEntities(before.getSpec().getEntity()))
                                .withChanges(List.of())
                                .build()
                        )
                        .build()
        );
        Assertions.assertEquals(expected, actual);
    }
}