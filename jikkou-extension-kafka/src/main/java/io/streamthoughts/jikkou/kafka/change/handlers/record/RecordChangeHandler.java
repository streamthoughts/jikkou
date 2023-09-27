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
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.kafka.change.RecordChange;
import io.streamthoughts.jikkou.kafka.internals.KafkaRecord;
import io.streamthoughts.jikkou.kafka.internals.producer.KafkaRecordSender;
import io.streamthoughts.jikkou.kafka.model.DataFormat;
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

public final class RecordChangeHandler implements ChangeHandler<RecordChange> {

    private final Producer<byte[], byte[]> producer;

    /**
     * Creates a new {@link RecordChangeHandler} instance.
     *
     * @param producer   the Producer.
     */
    public RecordChangeHandler(@NotNull Producer<byte[], byte[]> producer) {
        this.producer = Objects.requireNonNull(producer, "producerFactory must not be null");
    }

    /** {@inheritDoc} **/
    @Override
    public Set<ChangeType> supportedChangeTypes() {
        return Set.of(ChangeType.ADD, ChangeType.UPDATE);
    }

    /** {@inheritDoc} **/
    @Override
    public ChangeDescription getDescriptionFor(@NotNull HasMetadataChange<RecordChange> item) {
        return new RecordChangeDescription(item);
    }

    /** {@inheritDoc} **/
    @Override
    public List<ChangeResponse<RecordChange>> apply(@NotNull List<HasMetadataChange<RecordChange>> items) {
        KafkaRecordSender<byte[], byte[]> sender = new KafkaRecordSender<>(producer);
        return items.stream()
                .map(item -> send(item, sender))
                .collect(Collectors.toList());
    }

    @NotNull
    private static ChangeResponse<RecordChange> send(HasMetadataChange<RecordChange> item,
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
    static KafkaRecord<ByteBuffer, ByteBuffer> toKafkaRecord(final HasMetadataChange<RecordChange> item) {
        RecordChange change = item.getChange();
        DataFormat keyFormat = change.getKeyFormat();

        ChangeType changeType = change.getChangeType();
        Optional<ByteBuffer> rawKey = keyFormat.getDataSerde().serialize(
                change.getTopic(),
                change.getRecord().getAfter().getKey(),
                Collections.emptyMap(),
                true
        );

        Optional<ByteBuffer> rawValue = Optional.empty();

        if (changeType != ChangeType.DELETE) {
            DataFormat valueFormat = change.getValueFormat();
            rawValue = valueFormat.getDataSerde().serialize(
                    change.getTopic(),
                    change.getRecord().getAfter().getValue(),
                    Collections.emptyMap(),
                    false
            );
        }

        List<Header> headers = change.getRecord()
                .getAfter().getHeaders()
                .stream().map(h -> new RecordHeader(h.key(), h.value().getBytes(StandardCharsets.UTF_8)))
                .collect(Collectors.toList());

        return KafkaRecord
                .<ByteBuffer, ByteBuffer>builder()
                .topic(change.getTopic())
                .headers(new RecordHeaders(headers))
                .key(rawKey.orElse(null))
                .value(rawValue.orElse(null))
                .build();
    }


}
