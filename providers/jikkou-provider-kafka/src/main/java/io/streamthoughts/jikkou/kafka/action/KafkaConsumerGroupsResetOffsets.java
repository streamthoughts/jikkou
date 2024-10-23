/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.action;

import static io.streamthoughts.jikkou.core.config.ConfigPropertySpec.NULL_VALUE;

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.action.*;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionOptionSpec;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionSpec;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;
import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaConsumerGroupService;
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
    """
)
@ExtensionSpec(
    options = {
        @ExtensionOptionSpec(
            name = KafkaConsumerGroupsResetOffsets.TOPIC,
            description = "The topic whose partitions must be included in the reset-offset action.",
            type = List.class,
            required = true
        ),
        @ExtensionOptionSpec(
            name = KafkaConsumerGroupsResetOffsets.GROUP,
            description = "The consumer group to act on.",
            type = String.class
        ),
        @ExtensionOptionSpec(
            name = KafkaConsumerGroupsResetOffsets.GROUPS,
            description = "The consumer groups to act on.",
            type = List.class
        ),
        @ExtensionOptionSpec(
            name = KafkaConsumerGroupsResetOffsets.ALL,
            description = "Specifies to act on all consumer groups.",
            type = Boolean.class,
            defaultValue = "false"
        ),
        @ExtensionOptionSpec(
            name = KafkaConsumerGroupsResetOffsets.INCLUDES,
            description = "List of patterns to match the consumer groups that must be included in the reset-offset action.",
            type = List.class
        ),
        @ExtensionOptionSpec(
            name = KafkaConsumerGroupsResetOffsets.EXCLUDES,
            description = "List of patterns to match the consumer groups that must be excluded from the reset-offset action.",
            type = List.class
        ),
        @ExtensionOptionSpec(
            name = KafkaConsumerGroupsResetOffsets.TO_DATETIME,
            description = "Reset offsets to offset from datetime. Format: 'YYYY-MM-DDTHH:mm:SS.sss'",
            type = String.class,
            defaultValue = NULL_VALUE
        ),
        @ExtensionOptionSpec(
            name = KafkaConsumerGroupsResetOffsets.TO_EARLIEST,
            description = "Reset offsets to earliest offset.",
            type = Boolean.class,
            defaultValue = NULL_VALUE
        ),
        @ExtensionOptionSpec(
            name = KafkaConsumerGroupsResetOffsets.TO_LATEST,
            description = "Reset offsets to latest offset.",
            type = Boolean.class,
            defaultValue = NULL_VALUE
        ),
        @ExtensionOptionSpec(
            name = KafkaConsumerGroupsResetOffsets.TO_OFFSET,
            description = "Reset offsets to a specific offset.",
            type = Long.class,
            defaultValue = NULL_VALUE
        ),
        @ExtensionOptionSpec(
            name = KafkaConsumerGroupsResetOffsets.DRY_RUN,
            description = "Only show results without executing changes on Consumer Groups.",
            type = Boolean.class,
            defaultValue = "false"
        )
    }
)
@SupportedResource(type = V1KafkaConsumerGroup.class)
public final class KafkaConsumerGroupsResetOffsets extends ContextualExtension implements Action<V1KafkaConsumerGroup> {

    // OPTIONS
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerGroupsResetOffsets.class);
    public static final String TO_EARLIEST = "to-earliest";
    public static final String TO_LATEST = "to-latest";
    public static final String TO_DATETIME = "to-datetime";
    public static final String TOPIC = "topic";
    public static final String GROUP = "group";
    public static final String GROUPS = "groups";
    public static final String ALL = "all";
    public static final String INCLUDES = "includes";
    public static final String EXCLUDES = "excludes";
    public static final String TO_OFFSET = "to-offset";
    public static final String DRY_RUN = "dry-run";

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
            KafkaConsumerGroupService service = new KafkaConsumerGroupService(client);

            KafkaOffsetSpec offsetSpec = null;

            offsetSpec = extensionContext().<Boolean>configProperty(TO_EARLIEST)
                .getOptional(configuration)
                .map(unused -> (KafkaOffsetSpec) new KafkaOffsetSpec.ToEarliest())
                .orElse(offsetSpec);

            offsetSpec = extensionContext().<Boolean>configProperty(TO_LATEST)
                .getOptional(configuration)
                .map(unused -> (KafkaOffsetSpec) new KafkaOffsetSpec.ToLatest())
                .orElse(offsetSpec);

            offsetSpec = extensionContext().<String>configProperty(TO_DATETIME)
                .getOptional(configuration)
                .filter(Predicate.not(Strings::isBlank))
                .map(dataTime -> (KafkaOffsetSpec) KafkaOffsetSpec.ToTimestamp.fromISODateTime(dataTime))
                .orElse(offsetSpec);

            offsetSpec = extensionContext().<Long>configProperty(TO_OFFSET)
                .getOptional(configuration)
                .map(offset -> (KafkaOffsetSpec) new KafkaOffsetSpec.ToOffset(offset))
                .orElse(offsetSpec);

            if (offsetSpec == null) {
                return ExecutionResultSet.<V1KafkaConsumerGroup>newBuilder()
                    .result(ExecutionResult.<V1KafkaConsumerGroup>newBuilder()
                        .status(ExecutionStatus.FAILED)
                        .errors(List.of(new ExecutionError("No reset specification for offsets: "
                            + "One of these options is expected: "
                            + "[to-datetime, by-duration, to-earliest, to-latest, to-offset]."
                        )))
                        .build()
                    )
                    .build();
            }

            // get all groups
            final Stream<String> group = extensionContext().<String>configProperty(GROUP)
                .getOptional(configuration).stream();

            final Stream<String> groups = extensionContext().<List<String>>configProperty(GROUPS)
                .getOptional(configuration).stream().flatMap(Collection::stream);

            final Stream<String> all = extensionContext().<Boolean>configProperty(ALL).get(configuration) ?
                service.listConsumerGroups(Set.of(ConsumerGroupState.EMPTY), false)
                    .stream()
                    .map(it -> it.getMetadata().getName()) : Stream.empty();

            // get includes patterns
            final List<Pattern> includes = extensionContext().<List<String>>configProperty(INCLUDES)
                .getOptional(configuration).stream().flatMap(Collection::stream)
                .map(Pattern::compile)
                .toList();

            // get excludes patterns
            final List<Pattern> excludes = extensionContext().<List<String>>configProperty(EXCLUDES)
                .getOptional(configuration).stream().flatMap(Collection::stream)
                .map(Pattern::compile)
                .toList();

            // merge and filter all consumer groups.
            final List<String> groupIds = Stream.of(group, groups, all).flatMap(Function.identity())
                .filter(id -> isGroupIncluded(id, excludes, includes))
                .toList();

            final KafkaOffsetSpec offset = offsetSpec;
            final List<ExecutionResult<V1KafkaConsumerGroup>> results = groupIds.stream()
                .map(groupId -> {
                    try {
                        final List<String> topics = extensionContext().<List<String>>configProperty(TOPIC)
                            .get(configuration);

                        final Boolean dryRun = extensionContext().<Boolean>configProperty(DRY_RUN)
                            .get(configuration);

                        if (LOG.isInfoEnabled()) {
                            LOG.info("Alter consumer group '{}' for topics '{}', and offsets: {} (DRY_RUN: {}).",
                                groupId,
                                topics,
                                offset,
                                dryRun
                            );
                        }
                        V1KafkaConsumerGroup consumerGroupOffsets = service.resetConsumerGroupOffsets(
                            groupId,
                            topics,
                            offset,
                            dryRun
                        );

                        return ExecutionResult.<V1KafkaConsumerGroup>newBuilder()
                            .status(ExecutionStatus.SUCCEEDED)
                            .data(consumerGroupOffsets)
                            .build();
                    } catch (Exception ex) {
                        return ExecutionResult.<V1KafkaConsumerGroup>newBuilder()
                            .status(ExecutionStatus.FAILED)
                            .errors(List.of(new ExecutionError(ex.getLocalizedMessage())))
                            .build();
                    }
                })
                .toList();
            return ExecutionResultSet.<V1KafkaConsumerGroup>newBuilder().results(results).build();
        }
    }

    private static boolean isGroupIncluded(final String id, final List<Pattern> excludes, final List<Pattern> includes) {
        boolean isExcluded = excludes.stream().anyMatch(p -> p.matcher(id).matches());
        boolean isIncluded = includes.isEmpty() || includes.stream().anyMatch(p -> p.matcher(id).matches());
        return isIncluded && !isExcluded;
    }
}
