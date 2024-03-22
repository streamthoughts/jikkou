/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.change.topic;

import static io.streamthoughts.jikkou.extension.aiven.adapter.KafkaTopicAdapter.TAG_AIVEN_IO_PREFIX;
import static io.streamthoughts.jikkou.kafka.change.topics.TopicChange.CONFIG_PREFIX;
import static io.streamthoughts.jikkou.kafka.change.topics.TopicChange.PARTITIONS;
import static io.streamthoughts.jikkou.kafka.change.topics.TopicChange.REPLICAS;

import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.models.change.StateChangeList;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicInfoUpdate;
import io.streamthoughts.jikkou.extension.aiven.api.data.Tag;
import io.streamthoughts.jikkou.extension.aiven.change.AbstractChangeHandler;
import io.streamthoughts.jikkou.extension.aiven.change.KafkaChangeDescriptions;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public abstract class KafkaTopicChangeHandler extends AbstractChangeHandler {
    public KafkaTopicChangeHandler(@NotNull AivenApiClient api, @NotNull Operation operation) {
        super(api, operation);
    }

    public static class Create extends KafkaTopicChangeHandler {

        public Create(@NotNull AivenApiClient api) {
            super(api, Operation.CREATE);
        }

        @Override
        public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                .map(change -> executeAsync(
                    change,
                    () -> {
                        KafkaTopicInfoUpdate entry = topKafkaTopicInfoUpdate(change);
                        return api.updateKafkaTopicInfo(entry.topicName(), entry);
                    })
                )
                .collect(Collectors.toList());
        }
    }

    public static class Update extends KafkaTopicChangeHandler {

        public Update(@NotNull AivenApiClient api) {
            super(api, Operation.UPDATE);
        }

        @Override
        public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                .map(change -> executeAsync(
                    change,
                    () -> {
                        KafkaTopicInfoUpdate entry = topKafkaTopicInfoUpdate(change);
                        return api.updateKafkaTopicInfo(entry.topicName(), entry);
                    })
                )
                .collect(Collectors.toList());
        }
    }

    public static class Delete extends KafkaTopicChangeHandler {

        public Delete(@NotNull AivenApiClient api) {
            super(api, Operation.DELETE);
        }

        @Override
        public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                .map(change -> executeAsync(
                    change,
                    () -> {
                        KafkaTopicInfoUpdate entry = topKafkaTopicInfoUpdate(change);
                        return api.updateKafkaTopicInfo(entry.topicName(), entry);
                    })
                )
                .collect(Collectors.toList());
        }
    }

    public static class None extends ChangeHandler.None<ResourceChange> {
        public None() {
            super(change -> KafkaChangeDescriptions.of(change.getSpec().getOp(), getEntry(change, KafkaTopicInfoUpdate.class)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextDescription describe(@NotNull ResourceChange change) {
        return KafkaChangeDescriptions.of(change.getSpec().getOp(), getEntry(change, KafkaTopicInfoUpdate.class));
    }

    public KafkaTopicInfoUpdate topKafkaTopicInfoUpdate(final ResourceChange change) {
        final List<Tag> tags = change.getMetadata().getLabels().entrySet()
            .stream()
            .filter(it -> it.getKey().startsWith(TAG_AIVEN_IO_PREFIX))
            .map(it -> {
                String name = it.getKey().split("/", 2)[1];
                return new Tag(name, it.getValue().toString());
            }).toList();

        StateChangeList<? extends StateChange> data = change.getSpec().getChanges();
        Map<String, Object> configs = data.allWithPrefix(CONFIG_PREFIX)
            .stream()
            .map(it -> Pair.of(it.getName(), it))
            .map(it -> it.mapRight(StateChange::getAfter))
            .map(it -> it.mapRight(Object::toString))
            .collect(Collectors.toMap(Pair::_1, Pair::_2));

        return new KafkaTopicInfoUpdate(
            change.getMetadata().getName(),
            data.getLast(PARTITIONS, TypeConverter.Integer()).getAfter(),
            data.getLast(REPLICAS, TypeConverter.Integer()).getAfter(),
            configs,
            tags
        );
    }
}
