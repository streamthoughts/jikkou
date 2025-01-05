/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.action;

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.action.*;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;
import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaAdminService;
import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaOffsetSpec;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.ConsumerGroupState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("KafkaConsumerGroupsResetOffsets")
@Title("Reset offsets of consumer groups.")
@Description("""
    Reset offsets of consumer group. Supports multiple consumer groups, and groups should be in EMPTY state.
    You must choose one of the following reset specifications: to-datetime, by-duration, to-earliest, to-latest, to-offset.
    """)
@SupportedResource(type = V1KafkaConsumerGroup.class)
public final class KafkaConsumerGroupsResetOffsets extends ContextualExtension implements Action<V1KafkaConsumerGroup> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerGroupsResetOffsets.class);

    interface Config {
        ConfigProperty<Boolean> TO_EARLIEST = ConfigProperty
            .ofBoolean("to-earliest")
            .description("Reset offsets to earliest offset.");

        ConfigProperty<Boolean> TO_LATEST = ConfigProperty
            .ofBoolean("to-latest")
            .description("Reset offsets to latest offset.");

        ConfigProperty<Boolean> ALL = ConfigProperty
            .ofBoolean("all")
            .description("Specifies to act on all consumer groups.");

        ConfigProperty<String> GROUP = ConfigProperty
            .ofString("group")
            .description("The consumer group to act on.");

        ConfigProperty<List<String>> GROUPS = ConfigProperty
            .ofList("groups")
            .description("The consumer groups to act on.");

        ConfigProperty<List<String>> TOPIC = ConfigProperty.ofList("topic")
            .description("The topic whose partitions must be included in the reset-offset action.");

        ConfigProperty<List<String>> INCLUDES = ConfigProperty
            .ofList("includes")
            .description("List of patterns to match the consumer groups that must be included in the reset-offset action.");

        ConfigProperty<List<String>> EXCLUDES = ConfigProperty
            .ofList("excludes")
            .description("List of patterns to match the consumer groups that must be excluded from the reset-offset action.");

        ConfigProperty<String> TO_DATETIME = ConfigProperty
            .ofString("to-datetime")
            .description("Reset offsets to offset from datetime. Format: 'YYYY-MM-DDTHH:mm:SS.sss'");

        ConfigProperty<Long> TO_OFFSET = ConfigProperty
            .ofLong("to-offset")
            .description("Reset offsets to a specific offset.");

        ConfigProperty<Boolean> DRY_RUN = ConfigProperty
            .ofBoolean("dry-run")
            .description("Only show results without executing changes on Consumer Groups.")
            .defaultValue(false);
    }

    private AdminClientFactory adminClientFactory;

    /**
     * Creates a new {@link KafkaConsumerGroupsResetOffsets} instance.
     * CLI requires any empty constructor.
     */
    public KafkaConsumerGroupsResetOffsets() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        this.adminClientFactory = context.<KafkaExtensionProvider>provider().newAdminClientFactory();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull ExecutionResultSet<V1KafkaConsumerGroup> execute(@NotNull final Configuration configuration) {

        try (AdminClient client = adminClientFactory.createAdminClient()) {
            KafkaAdminService service = new KafkaAdminService(client);

            KafkaOffsetSpec offsetSpec = null;

            offsetSpec = Config.TO_EARLIEST
                .getOptional(configuration)
                .map(unused -> (KafkaOffsetSpec) new KafkaOffsetSpec.ToEarliest())
                .orElse(offsetSpec);

            offsetSpec = Config.TO_LATEST
                .getOptional(configuration)
                .map(unused -> (KafkaOffsetSpec) new KafkaOffsetSpec.ToLatest())
                .orElse(offsetSpec);

            offsetSpec = Config.TO_DATETIME
                .getOptional(configuration)
                .filter(Predicate.not(Strings::isBlank))
                .map(dataTime -> (KafkaOffsetSpec) KafkaOffsetSpec.ToTimestamp.fromISODateTime(dataTime))
                .orElse(offsetSpec);

            offsetSpec = Config.TO_OFFSET
                .getOptional(configuration)
                .map(offset -> (KafkaOffsetSpec) new KafkaOffsetSpec.ToOffset(offset))
                .orElse(offsetSpec);

            if (offsetSpec == null) {
                return ExecutionResultSet.<V1KafkaConsumerGroup>newBuilder()
                    .result(
                        ExecutionResult.<V1KafkaConsumerGroup>newBuilder()
                        .status(ExecutionStatus.FAILED)
                        .errors(List.of(new ExecutionError("No reset specification for offsets: " + "One of these options is expected: " + "[to-datetime, by-duration, to-earliest, to-latest, to-offset].")))
                        .build()
                    )
                    .build();
            }

            // get all groups
            final Stream<String> group = Config.GROUP.getOptional(configuration).stream();

            final Stream<String> groups = Config.GROUPS.getOptional(configuration).stream().flatMap(Collection::stream);

            final Stream<String> all = Config.ALL.get(configuration) ?
                service.listConsumerGroups(Set.of(ConsumerGroupState.EMPTY), false)
                    .stream()
                    .map(it -> it.getMetadata().getName()) : Stream.empty();

            // get includes patterns
            final List<Pattern> includes = Config.INCLUDES
                .getOptional(configuration).stream().flatMap(Collection::stream)
                .map(Pattern::compile)
                .toList();

            // get excludes patterns
            final List<Pattern> excludes = Config.EXCLUDES
                .getOptional(configuration)
                .stream()
                .flatMap(Collection::stream)
                .map(Pattern::compile)
                .toList();

            // merge and filter all consumer groups.
            final List<String> groupIds = Stream.of(group, groups, all)
                .flatMap(Function.identity())
                .filter(id -> isGroupIncluded(id, excludes, includes))
                .toList();

            final KafkaOffsetSpec offset = offsetSpec;
            final List<ExecutionResult<V1KafkaConsumerGroup>> results = groupIds.stream().map(groupId -> {
                try {
                    final List<String> topics = Config.TOPIC.get(configuration);

                    final Boolean dryRun = Config.DRY_RUN.get(configuration);

                    if (LOG.isInfoEnabled()) {
                        LOG.info("Alter consumer group '{}' for topics '{}', and offsets: {} (DRY_RUN: {}).", groupId, topics, offset, dryRun);
                    }
                    V1KafkaConsumerGroup consumerGroupOffsets = service.resetConsumerGroupOffsets(groupId, topics, offset, dryRun);

                    return ExecutionResult.<V1KafkaConsumerGroup>newBuilder().status(ExecutionStatus.SUCCEEDED).data(consumerGroupOffsets).build();
                } catch (Exception ex) {
                    return ExecutionResult.<V1KafkaConsumerGroup>newBuilder().status(ExecutionStatus.FAILED).errors(List.of(new ExecutionError(ex.getLocalizedMessage()))).build();
                }
            }).toList();
            return ExecutionResultSet.<V1KafkaConsumerGroup>newBuilder().results(results).build();
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(
            Config.TO_EARLIEST,
            Config.TO_LATEST,
            Config.TO_DATETIME,
            Config.TO_OFFSET,
            Config.GROUP,
            Config.GROUPS,
            Config.TOPIC,
            Config.ALL,
            Config.INCLUDES,
            Config.EXCLUDES,
            Config.DRY_RUN
        );
    }

    private static boolean isGroupIncluded(final String id, final List<Pattern> excludes, final List<Pattern> includes) {
        boolean isExcluded = excludes.stream().anyMatch(p -> p.matcher(id).matches());
        boolean isIncluded = includes.isEmpty() || includes.stream().anyMatch(p -> p.matcher(id).matches());
        return isIncluded && !isExcluded;
    }
}
