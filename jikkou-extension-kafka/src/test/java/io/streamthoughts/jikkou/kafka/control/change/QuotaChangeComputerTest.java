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
package io.streamthoughts.jikkou.kafka.control.change;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaConfigs;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaType;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaEntity;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaSpec;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QuotaChangeComputerTest {

    public static final List<V1KafkaClientQuota> EMPTY_LIST = Collections.emptyList();

    @Test
    void shouldReturnAddChangeForNewQuota() {
        // Given
        QuotaChangeComputer computer = new QuotaChangeComputer();
        computer.isLimitDeletionEnabled(true);

        V1KafkaClientQuota expected = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(V1KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .build()
                )
                .build();

        // When
        List<QuotaChange> changes = computer.computeChanges(EMPTY_LIST, List.of(expected));

        // Then
        Assertions.assertNotNull(changes);
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.ADD, changes.get(0).getChangeType());
    }

    @Test
    void shouldReturnNoneChangeForEqualsQuota() {
        // Given
        QuotaChangeComputer computer = new QuotaChangeComputer();
        computer.isLimitDeletionEnabled(true);

        V1KafkaClientQuota expected = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(V1KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .build()
                )
                .build();

        // When
        List<QuotaChange> changes = computer.computeChanges(List.of(expected), List.of(expected));

        // Then
        Assertions.assertNotNull(changes);
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.NONE, changes.get(0).getChangeType());
    }

    @Test
    void shouldReturnDeleteChangeForExistingQuotaDeleteTrue() {
        // Given
        QuotaChangeComputer computer = new QuotaChangeComputer();
        computer.isLimitDeletionEnabled(true);

        V1KafkaClientQuota actual = V1KafkaClientQuota.builder()
                .withMetadata(ObjectMeta.builder()
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_DELETE, true)
                        .build()
                )
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(V1KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .build()
                )
                .build();

        // When
        List<QuotaChange> changes = computer.computeChanges(List.of(actual), List.of(actual));

        // Then
        Assertions.assertNotNull(changes);
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.DELETE, changes.get(0).getChangeType());
    }

    @Test
    void shouldReturnNoDeleteChangeForNotExistingQuotaDeleteTrue() {
        // Given
        QuotaChangeComputer computer = new QuotaChangeComputer();
        computer.isLimitDeletionEnabled(true);

        V1KafkaClientQuota actual = V1KafkaClientQuota.builder()
                .withMetadata(ObjectMeta.builder()
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_DELETE, true)
                        .build()
                )
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(V1KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .build()
                )
                .build();

        // When
        List<QuotaChange> changes = computer.computeChanges(EMPTY_LIST, List.of(actual));

        // Then
        Assertions.assertNotNull(changes);
        Assertions.assertTrue(changes.isEmpty());
    }

    @Test
    void shouldReturnUpdateChangeForExistingQuota() {
        // Given
        QuotaChangeComputer computer = new QuotaChangeComputer();
        computer.isLimitDeletionEnabled(true);

        V1KafkaClientQuota expected = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(V1KafkaClientQuotaEntity
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

        V1KafkaClientQuota actual = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(V1KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test")
                                .build()
                        )
                        .withConfigs(KafkaClientQuotaConfigs
                                .builder()
                                .withConsumerByteRate(OptionalDouble.of(0.0))
                                .build()
                        )
                        .build()
                )
                .build();

        // When
        List<QuotaChange> changes = computer.computeChanges(List.of(actual), List.of(expected));

        // Then
        Assertions.assertNotNull(changes);
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.UPDATE, changes.get(0).getChangeType());
    }

    @Test
    void shouldReturnUpdateChangeWithDeleteLimitForDeleteEnableTrue() {
        // Given
        QuotaChangeComputer computer = new QuotaChangeComputer();
        computer.isLimitDeletionEnabled(true);

        V1KafkaClientQuota actual = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(V1KafkaClientQuotaEntity
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

        V1KafkaClientQuota expected = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(V1KafkaClientQuotaEntity
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
        List<QuotaChange> changes = computer.computeChanges(List.of(actual), List.of(expected));

        // Then
        Assertions.assertNotNull(changes);
        Assertions.assertEquals(1, changes.size());
        QuotaChange quotaChange = changes.get(0);

        Assertions.assertEquals(ChangeType.UPDATE, quotaChange.getChangeType());
        Assertions.assertEquals(1, quotaChange.getConfigEntryChanges().size());
        Assertions.assertEquals(ChangeType.DELETE, quotaChange.getConfigEntryChanges().get(0).getChangeType());
    }

    @Test
    void shouldReturnNoneChangeWithNoDeleteLimitForDeleteEnableFalse() {
        // Given
        QuotaChangeComputer computer = new QuotaChangeComputer();
        computer.isLimitDeletionEnabled(false);

        V1KafkaClientQuota actual = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(V1KafkaClientQuotaEntity
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

        V1KafkaClientQuota expected = V1KafkaClientQuota.builder()
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(V1KafkaClientQuotaEntity
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
        List<QuotaChange> changes = computer.computeChanges(List.of(actual), List.of(expected));

        // Then
        Assertions.assertNotNull(changes);
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.NONE, changes.get(0).getChangeType());
    }
}