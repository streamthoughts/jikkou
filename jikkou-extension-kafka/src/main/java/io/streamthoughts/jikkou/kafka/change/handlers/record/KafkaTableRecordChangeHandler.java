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
package io.streamthoughts.jikkou.kafka.change.handlers.record;

import io.streamthoughts.jikkou.api.change.ChangeDescription;
import io.streamthoughts.jikkou.api.change.ChangeHandler;
import io.streamthoughts.jikkou.api.change.ChangeMetadata;
import io.streamthoughts.jikkou.api.change.ChangeResponse;
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.kafka.change.KafkaTableRecordChange;
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

/**
 * Handler for {@link KafkaTableRecordChange}.
 */
public final class KafkaTableRecordChangeHandler implements ChangeHandler<KafkaTableRecordChange> {

    private final Producer<byte[], byte[]> producer;

    /**
     * Creates a new {@link KafkaTableRecordChangeHandler} instance.
     *
     * @param producer   the Producer.
     */
    public KafkaTableRecordChangeHandler(@NotNull Producer<byte[], byte[]> producer) {
        this.producer = Objects.requireNonNull(producer, "producerFactory must not be null");
    }

    /** {@inheritDoc} **/
    @Override
    public Set<ChangeType> supportedChangeTypes() {
        return Set.of(ChangeType.ADD, ChangeType.UPDATE);
    }

    /** {@inheritDoc} **/
    @Override
    public ChangeDescription getDescriptionFor(@NotNull HasMetadataChange<KafkaTableRecordChange> item) {
        return new KafkaTableRecordChangeDescription(item);
    }

    /** {@inheritDoc} **/
    @Override
    public List<ChangeResponse<KafkaTableRecordChange>> apply(@NotNull List<HasMetadataChange<KafkaTableRecordChange>> items) {
        KafkaRecordSender<byte[], byte[]> sender = new KafkaRecordSender<>(producer);
        return items.stream()
                .map(item -> send(item, sender))
                .collect(Collectors.toList());
    }

    @NotNull
    private static ChangeResponse<KafkaTableRecordChange> send(HasMetadataChange<KafkaTableRecordChange> item,
                                                               KafkaRecordSender<byte[], byte[]> sender) {

        KafkaRecord<byte[], byte[]> record = toKafkaRecord(item)
                .mapKey(k -> Optional.ofNullable(k).map(ByteBuffer::array).orElse(null))
                .mapValue(v -> Optional.ofNullable(v).map(ByteBuffer::array).orElse(null));

        CompletableFuture<ChangeMetadata> future = sender.send(record)
                .thenApply(result -> result.error() != null ?
                        ChangeMetadata.of(result.error()) :
                        ChangeMetadata.empty()
                );
        return new ChangeResponse<>(item, future);
    }

    @VisibleForTesting
    static KafkaRecord<ByteBuffer, ByteBuffer> toKafkaRecord(final HasMetadataChange<KafkaTableRecordChange> item) {
        KafkaTableRecordChange change = item.getChange();
        ChangeType changeType = change.operation();

        ValueChange<V1KafkaTableRecordSpec> spec = change.getRecord();

        DataValue key = changeType == ChangeType.ADD ?
                spec.getAfter().getKey() :
                spec.getBefore().getKey();

        final String topic = change.getTopic();
        Optional<ByteBuffer> rawKey = key.type().getDataSerde().serialize(
                topic,
                key.data(),
                Collections.emptyMap(),
                true
        );

        Optional<ByteBuffer> rawValue = Optional.empty();
        if (changeType != ChangeType.DELETE) {
            DataValue value = spec.getAfter().getValue();
            rawValue = value.type().getDataSerde().serialize(
                    topic,
                    value.data(),
                    Collections.emptyMap(),
                    false
            );
        }

        List<Header> headers = spec
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
