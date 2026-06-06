/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.action;

import io.jikkou.core.action.Action;
import io.jikkou.core.action.ExecutionError;
import io.jikkou.core.action.ExecutionResult;
import io.jikkou.core.action.ExecutionResultSet;
import io.jikkou.core.action.ExecutionStatus;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.Named;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.extension.ContextualExtension;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.kafka.KafkaExtensionProvider;
import io.jikkou.kafka.internals.admin.AdminClientFactory;
import io.jikkou.kafka.models.V1KafkaShareGroup;
import io.jikkou.kafka.reconciler.service.KafkaAdminService;
import io.jikkou.kafka.reconciler.service.KafkaOffsetSpec;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.GroupState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("KafkaShareGroupsResetOffsets")
@Title("Reset offsets of share groups.")
@Description("""
    Reset the Share-Partition Start Offset (SPSO) of share groups. Supports multiple share groups.
    Choose one of: to-earliest, to-latest, to-offset, or delete-offsets.
    """)
@SupportedResource(type = V1KafkaShareGroup.class)
public final class KafkaShareGroupsResetOffsets extends ContextualExtension implements Action<V1KafkaShareGroup> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaShareGroupsResetOffsets.class);

    interface Config {
        ConfigProperty<Boolean> TO_EARLIEST = ConfigProperty.ofBoolean("to-earliest")
            .displayName("To Earliest").description("Reset offsets to earliest offset.");
        ConfigProperty<Boolean> TO_LATEST = ConfigProperty.ofBoolean("to-latest")
            .displayName("To Latest").description("Reset offsets to latest offset.");
        ConfigProperty<Long> TO_OFFSET = ConfigProperty.ofLong("to-offset")
            .displayName("To Offset").description("Reset offsets to a specific offset.");
        ConfigProperty<Boolean> DELETE_OFFSETS = ConfigProperty.ofBoolean("delete-offsets")
            .displayName("Delete Offsets").description("Delete the share-partition offsets for the given topics.");
        ConfigProperty<Boolean> ALL = ConfigProperty.ofBoolean("all")
            .displayName("All Groups").description("Act on all share groups.");
        ConfigProperty<String> GROUP = ConfigProperty.ofString("group")
            .displayName("Group").description("The share group to act on.");
        ConfigProperty<List<String>> GROUPS = ConfigProperty.ofList("groups")
            .displayName("Groups").description("The share groups to act on.");
        ConfigProperty<List<String>> TOPIC = ConfigProperty.ofList("topic")
            .displayName("Topic").description("Topics to include. Each entry is 'topic' or 'topic:partition'.");
        ConfigProperty<List<String>> INCLUDES = ConfigProperty.ofList("includes")
            .displayName("Includes").description("Patterns of share groups to include.");
        ConfigProperty<List<String>> EXCLUDES = ConfigProperty.ofList("excludes")
            .displayName("Excludes").description("Patterns of share groups to exclude.");
        ConfigProperty<Boolean> DRY_RUN = ConfigProperty.ofBoolean("dry-run")
            .displayName("Dry Run").description("Only show results without executing changes.").defaultValue(false);
    }

    private AdminClientFactory adminClientFactory;

    /**
     * Creates a new {@link KafkaShareGroupsResetOffsets} instance.
     * CLI requires any empty constructor.
     */
    public KafkaShareGroupsResetOffsets() {
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
     */
    @Override
    public @NotNull ExecutionResultSet<V1KafkaShareGroup> execute(@NotNull Configuration configuration) {
        try (AdminClient client = adminClientFactory.createAdminClient()) {
            KafkaAdminService service = new KafkaAdminService(client);

            boolean deleteOffsets = Config.DELETE_OFFSETS.getOptional(configuration).orElse(false);
            KafkaOffsetSpec offsetSpec = null;
            offsetSpec = Config.TO_EARLIEST.getOptional(configuration)
                .map(u -> (KafkaOffsetSpec) new KafkaOffsetSpec.ToEarliest()).orElse(offsetSpec);
            offsetSpec = Config.TO_LATEST.getOptional(configuration)
                .map(u -> (KafkaOffsetSpec) new KafkaOffsetSpec.ToLatest()).orElse(offsetSpec);
            offsetSpec = Config.TO_OFFSET.getOptional(configuration)
                .map(o -> (KafkaOffsetSpec) new KafkaOffsetSpec.ToOffset(o)).orElse(offsetSpec);

            if (offsetSpec == null && !deleteOffsets) {
                return ExecutionResultSet.<V1KafkaShareGroup>newBuilder()
                    .result(ExecutionResult.<V1KafkaShareGroup>newBuilder()
                        .status(ExecutionStatus.FAILED)
                        .errors(List.of(new ExecutionError(
                            "No reset specification. Expected one of: [to-earliest, to-latest, to-offset, delete-offsets].")))
                        .build())
                    .build();
            }

            Stream<String> group = Config.GROUP.getOptional(configuration).stream();
            Stream<String> groups = Config.GROUPS.getOptional(configuration).stream().flatMap(Collection::stream);
            Stream<String> all = Config.ALL.getOptional(configuration).orElse(false)
                ? service.listShareGroups(Set.<GroupState>of(), false).stream().map(it -> it.getMetadata().getName())
                : Stream.empty();

            List<Pattern> includes = Config.INCLUDES.getOptional(configuration).stream()
                .flatMap(Collection::stream).map(Pattern::compile).toList();
            List<Pattern> excludes = Config.EXCLUDES.getOptional(configuration).stream()
                .flatMap(Collection::stream).map(Pattern::compile).toList();

            List<String> groupIds = Stream.of(group, groups, all)
                .flatMap(Function.identity())
                .filter(id -> isIncluded(id, excludes, includes))
                .toList();

            final KafkaOffsetSpec spec = offsetSpec;
            final boolean dryRun = Config.DRY_RUN.get(configuration);
            final List<String> topics = Config.TOPIC.get(configuration);

            List<ExecutionResult<V1KafkaShareGroup>> results = groupIds.stream().map(groupId -> {
                try {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Reset share group '{}' for topics '{}' spec={} (DRY_RUN: {}).",
                            groupId, topics, spec, dryRun);
                    }
                    V1KafkaShareGroup result = service.resetShareGroupOffsets(groupId, topics, spec, dryRun);
                    return ExecutionResult.<V1KafkaShareGroup>newBuilder()
                        .status(ExecutionStatus.SUCCEEDED).data(result).build();
                } catch (Exception ex) {
                    return ExecutionResult.<V1KafkaShareGroup>newBuilder()
                        .status(ExecutionStatus.FAILED)
                        .errors(List.of(new ExecutionError(ex.getLocalizedMessage()))).build();
                }
            }).toList();
            return ExecutionResultSet.<V1KafkaShareGroup>newBuilder().results(results).build();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(Config.TO_EARLIEST, Config.TO_LATEST, Config.TO_OFFSET, Config.DELETE_OFFSETS,
            Config.GROUP, Config.GROUPS, Config.TOPIC, Config.ALL, Config.INCLUDES, Config.EXCLUDES, Config.DRY_RUN);
    }

    private static boolean isIncluded(String id, List<Pattern> excludes, List<Pattern> includes) {
        boolean isExcluded = excludes.stream().anyMatch(p -> p.matcher(id).matches());
        boolean isIncluded = includes.isEmpty() || includes.stream().anyMatch(p -> p.matcher(id).matches());
        return isIncluded && !isExcluded;
    }
}
