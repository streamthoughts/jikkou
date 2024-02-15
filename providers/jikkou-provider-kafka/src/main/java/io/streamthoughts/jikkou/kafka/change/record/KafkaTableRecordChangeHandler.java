/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.record;

import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import io.streamthoughts.jikkou.kafka.internals.KafkaRecord;
import io.streamthoughts.jikkou.kafka.internals.producer.KafkaRecordSender;
import io.streamthoughts.jikkou.kafka.model.DataValue;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

public final class KafkaTableRecordChangeHandler extends BaseChangeHandler<ResourceChange> {

    private final Producer<byte[], byte[]> producer;

    /**
     * Creates a new {@link KafkaTableRecordChangeHandler} instance.
     *
     * @param producer the Producer.
     */
    public KafkaTableRecordChangeHandler(@NotNull Producer<byte[], byte[]> producer) {
        super(Set.of(Operation.CREATE, Operation.UPDATE));
        this.producer = Objects.requireNonNull(producer, "producerFactory must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public TextDescription describe(@NotNull ResourceChange change) {
        return new KafkaTableRecordChangeDescription(change);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull List<ResourceChange> changes) {
        KafkaRecordSender<byte[], byte[]> sender = new KafkaRecordSender<>(producer);
        return changes.stream()
                .map(item -> send(item, sender))
                .collect(Collectors.toList());
    }

    @NotNull
    private static ChangeResponse<ResourceChange> send(ResourceChange change,
                                                       KafkaRecordSender<byte[], byte[]> sender) {

        KafkaRecord<byte[], byte[]> record = toKafkaRecord(change)
                .mapKey(k -> Optional.ofNullable(k).map(ByteBuffer::array).orElse(null))
                .mapValue(v -> Optional.ofNullable(v).map(ByteBuffer::array).orElse(null));

        CompletableFuture<ChangeMetadata> future = sender.send(record)
                .thenApply(result -> result.error() != null ?
                        ChangeMetadata.of(result.error()) :
                        ChangeMetadata.empty()
                );
        return new ChangeResponse<>(change, future);
    }

    @VisibleForTesting
    static KafkaRecord<ByteBuffer, ByteBuffer> toKafkaRecord(final ResourceChange change) {
        SpecificStateChange<V1KafkaTableRecordSpec> data = change.getSpec()
                .getChanges()
                .getLast("record", TypeConverter.of(V1KafkaTableRecordSpec.class));
        Operation op = data.getOp();
        DataValue key = op == Operation.CREATE ?
                data.getAfter().getKey() :
                data.getBefore().getKey();

        final String topic = op == Operation.DELETE ?
                data.getBefore().getTopic() :
                data.getAfter().getTopic();
        Optional<ByteBuffer> rawKey = key.type().getDataSerde().serialize(
                topic,
                key.data(),
                Collections.emptyMap(),
                true
        );

        Optional<ByteBuffer> rawValue = Optional.empty();
        if (op != Operation.DELETE) {
            DataValue value = data.getAfter().getValue();
            rawValue = value.type().getDataSerde().serialize(
                    topic,
                    value.data(),
                    Collections.emptyMap(),
                    false
            );
        }

        List<Header> headers = data
                .getAfter().getHeaders()
                .stream().map(h -> new RecordHeader(h.name(), h.value().getBytes(StandardCharsets.UTF_8)))
                .collect(Collectors.toList());

        return KafkaRecord
                .<ByteBuffer, ByteBuffer>builder()
                .topic(topic)
                .headers(new RecordHeaders(headers))
                .key(rawKey.orElse(null))
                .value(rawValue.orElse(null))
                .build();
    }
}
