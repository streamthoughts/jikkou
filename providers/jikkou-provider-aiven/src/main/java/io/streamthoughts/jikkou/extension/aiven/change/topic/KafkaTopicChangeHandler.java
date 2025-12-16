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
import io.streamthoughts.jikkou.extension.aiven.adapter.KafkaTopicAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicInfoCreate;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicInfoUpdate;
import io.streamthoughts.jikkou.extension.aiven.api.data.Tag;
import io.streamthoughts.jikkou.extension.aiven.change.AbstractChangeHandler;
import io.streamthoughts.jikkou.kafka.change.topics.TopicChange;
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
        public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                .map(change -> executeAsync(
                    change,
                    () -> {
                        KafkaTopicInfoCreate entry = toKafkaTopicInfoCreate(change);
                        return api.createKafkaTopicInfo(entry);
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
        public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                .map(change -> executeAsync(
                    change,
                    () -> {
                        KafkaTopicInfoUpdate entry = toKafkaTopicInfoUpdate(change);
                        return api.updateKafkaTopicInfo(change.getMetadata().getName(), entry);
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
        public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                .map(change -> executeAsync(
                    change,
                    () -> api.deleteKafkaTopicInfo(change.getMetadata().getName()))
                )
                .collect(Collectors.toList());
        }
    }

    public static class None extends ChangeHandler.None{
        public None() {
            super(TopicChange::getDescription);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextDescription describe(@NotNull ResourceChange change) {
        return TopicChange.getDescription(change);
    }

    public KafkaTopicInfoCreate toKafkaTopicInfoCreate(final ResourceChange change) {
        StateChangeList<? extends StateChange> data = change.getSpec().getChanges();
        return new KafkaTopicInfoCreate(
            change.getMetadata().getName(),
            data.getLast(PARTITIONS, TypeConverter.Integer()).getAfter(),
            data.getLast(REPLICAS, TypeConverter.Integer()).getAfter(),
            getConfigFromResourceChange(change),
            getTagsFromResourceChange(change)
        );
    }

    public KafkaTopicInfoUpdate toKafkaTopicInfoUpdate(final ResourceChange change) {
        StateChangeList<? extends StateChange> data = change.getSpec().getChanges();
        return new KafkaTopicInfoUpdate(
            data.getLast(PARTITIONS, TypeConverter.Integer()).getAfter(),
            data.getLast(REPLICAS, TypeConverter.Integer()).getAfter(),
            getConfigFromResourceChange(change),
            getTagsFromResourceChange(change)
        );
    }

    private static @NotNull Map<String, Object> getConfigFromResourceChange(final ResourceChange change) {
        StateChangeList<? extends StateChange> data = change.getSpec().getChanges();
        return data.allWithPrefix(CONFIG_PREFIX)
            .stream()
            .filter(it -> it.getAfter() != null)
            .map(it -> Pair.of(it.getName(), it))
            .map(it -> it.mapLeft(KafkaTopicAdapter::configKeyToAiven))
            .map(it -> it.mapRight(StateChange::getAfter))
            .collect(Collectors.toMap(Pair::_1, Pair::_2));
    }

    private static @NotNull List<Tag> getTagsFromResourceChange(final ResourceChange change) {
        return change.getMetadata().getLabels().entrySet()
            .stream()
            .filter(it -> it.getKey().startsWith(TAG_AIVEN_IO_PREFIX))
            .map(it -> {
                String name = it.getKey().split("/", 2)[1];
                return new Tag(name, it.getValue().toString());
            }).toList();
    }
}
