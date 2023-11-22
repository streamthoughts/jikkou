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
package io.streamthoughts.jikkou.kafka.action;

import static io.streamthoughts.jikkou.core.config.ConfigPropertySpec.NULL_VALUE;

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.action.Action;
import io.streamthoughts.jikkou.core.action.ExecutionError;
import io.streamthoughts.jikkou.core.action.ExecutionResult;
import io.streamthoughts.jikkou.core.action.ExecutionResultSet;
import io.streamthoughts.jikkou.core.action.ExecutionStatus;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionOptionSpec;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionSpec;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientFactory;
import io.streamthoughts.jikkou.kafka.internals.admin.DefaultAdminClientFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;
import io.streamthoughts.jikkou.kafka.reconcilier.KafkaClientConfiguration;
import io.streamthoughts.jikkou.kafka.reconcilier.service.KafkaConsumerGroupService;
import io.streamthoughts.jikkou.kafka.reconcilier.service.KafkaOffsetSpec;
import java.util.List;
import java.util.function.Predicate;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Named("KafkaConsumerGroupsResetOffsets")
@Title("Reset offsets of consumer group.")
@Description("""
        Reset offsets of consumer group. Supports one consumer group at the time, and group should be in EMPTY state.
        You must choose one of the following reset specifications: to-datetime, by-duration, to-earliest, to-latest, to-offset.
        """
)
@ExtensionSpec(
        options = {
                @ExtensionOptionSpec(
                        name = KafkaConsumerGroupsResetOffsets.GROUP,
                        description = "The consumer group to act on.",
                        type = String.class,
                        required = true
                ),
                @ExtensionOptionSpec(
                        name = KafkaConsumerGroupsResetOffsets.TOPIC,
                        description = "The topic whose partitions must be included in the reset-offset action.",
                        type = List.class,
                        required = true
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
public final class KafkaConsumerGroupsResetOffsets implements Action<V1KafkaConsumerGroup> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerGroupsResetOffsets.class);
    public static final String TO_EARLIEST = "to-earliest";
    public static final String TO_LATEST = "to-latest";
    public static final String TO_DATETIME = "to-datetime";
    public static final String TOPIC = "topic";
    public static final String GROUP = "group";
    public static final String TO_OFFSET = "to-offset";
    public static final String DRY_RUN = "dry-run";

    private ExtensionContext context;

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
        this.context = context;
        this.adminClientFactory = new DefaultAdminClientFactory(() ->
                KafkaClientConfiguration.ADMIN_CLIENT_CONFIG.get(context.appConfiguration())
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull ExecutionResultSet<V1KafkaConsumerGroup> execute(@NotNull final Configuration configuration) {

        try (AdminClient client = adminClientFactory.createAdminClient()) {
            KafkaConsumerGroupService service = new KafkaConsumerGroupService(client);

            KafkaOffsetSpec offsetSpec = null;


            offsetSpec = context.<Boolean>configProperty(TO_EARLIEST)
                    .getOptional(configuration)
                    .map(unused -> (KafkaOffsetSpec) new KafkaOffsetSpec.ToEarliest())
                    .orElse(offsetSpec);

            offsetSpec = context.<Boolean>configProperty(TO_LATEST)
                    .getOptional(configuration)
                    .map(unused -> (KafkaOffsetSpec) new KafkaOffsetSpec.ToLatest())
                    .orElse(offsetSpec);

            offsetSpec = context.<String>configProperty(TO_DATETIME)
                    .getOptional(configuration)
                    .filter(Predicate.not(Strings::isBlank))
                    .map(dataTime -> (KafkaOffsetSpec) KafkaOffsetSpec.ToTimestamp.fromISODateTime(dataTime))
                    .orElse(offsetSpec);

            offsetSpec = context.<Long>configProperty(TO_OFFSET)
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

            try {
                final String groupId = context.<String>configProperty(GROUP)
                        .get(configuration);

                final List<String> topics = context.<List<String>>configProperty(TOPIC)
                        .get(configuration);

                final Boolean dryRun = context.<Boolean>configProperty(DRY_RUN)
                        .get(configuration);

                if (LOG.isInfoEnabled()) {
                    LOG.info("Alter consumer group '{}' for topics '{}', and offsets: {} (DRY_RUN: {}).",
                            groupId,
                            topics,
                            offsetSpec,
                            dryRun
                    );
                }
                V1KafkaConsumerGroup group = service.resetConsumerGroupOffsets(
                        groupId,
                        topics,
                        offsetSpec,
                        dryRun
                );
                return ExecutionResultSet.<V1KafkaConsumerGroup>newBuilder()
                        .result(ExecutionResult.<V1KafkaConsumerGroup>newBuilder()
                                .status(ExecutionStatus.SUCCEED)
                                .resource(group)
                                .build()
                        )
                        .build();
            } catch (Exception ex) {
                return ExecutionResultSet.<V1KafkaConsumerGroup>newBuilder()
                        .result(ExecutionResult.<V1KafkaConsumerGroup>newBuilder()
                                .status(ExecutionStatus.FAILED)
                                .errors(List.of(new ExecutionError(ex.getLocalizedMessage())))
                                .build()
                        )
                        .build();
            }
        }
    }
}
