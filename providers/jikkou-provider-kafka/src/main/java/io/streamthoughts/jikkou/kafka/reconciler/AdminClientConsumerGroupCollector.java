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
package io.streamthoughts.jikkou.kafka.reconciler;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionOptionSpec;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionSpec;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;
import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaConsumerGroupService;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.kafka.common.ConsumerGroupState;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1KafkaConsumerGroup.class)
@ExtensionSpec(
        options = {
                @ExtensionOptionSpec(
                        name = AdminClientConsumerGroupCollector.Config.OFFSETS_CONFIG_NAME,
                        description = AdminClientConsumerGroupCollector.Config.OFFSETS_DESCRIPTION,
                        type = Boolean.class
                ),
                @ExtensionOptionSpec(
                        name = AdminClientConsumerGroupCollector.Config.IN_STATE_CONFIG_NAME,
                        description = AdminClientConsumerGroupCollector.Config.IN_STATE_CONFIG_DESCRIPTION,
                        type = List.class
                )
        }
)
public final class AdminClientConsumerGroupCollector implements Collector<V1KafkaConsumerGroup> {

    private AdminClientContextFactory adminClientContextFactory;

    /**
     * Creates a new {@link AdminClientKafkaAclCollector} instance.
     * CLI requires any empty constructor.
     */
    public AdminClientConsumerGroupCollector() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaAclCollector} instance with the specified {@link AdminClientContext}.
     *
     * @param adminClientContextFactory the {@link AdminClientContextFactory} to use for acquiring a new {@link AdminClientContext}.
     */
    public AdminClientConsumerGroupCollector(final @NotNull AdminClientContextFactory adminClientContextFactory) {
        this.adminClientContextFactory = adminClientContextFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) {
        if (adminClientContextFactory == null) {
            this.adminClientContextFactory = new AdminClientContextFactory(context.appConfiguration());
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceListObject<V1KafkaConsumerGroup> listAll(@NotNull Configuration configuration,
                                                            @NotNull Selector selector) {
        final Config config = new Config(configuration);
        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            KafkaConsumerGroupService service = new KafkaConsumerGroupService(clientContext.getAdminClient());
            return service.listConsumerGroups(config.inStates(), config.describeOffsets());
        }
    }

    static class Config {

        public static final String OFFSETS_CONFIG_NAME = "offsets";
        public static final String OFFSETS_DESCRIPTION = "Specify whether consumer group offsets should be described.";
        public static final String IN_STATE_CONFIG_NAME = "in-states";

        private final static Map<String, ConsumerGroupState> NAME_TO_ENUM = Arrays.stream(ConsumerGroupState.values())
                .collect(Collectors.toMap(state -> state.name().toUpperCase(Locale.ROOT), Function.identity()));

        public static final String IN_STATE_CONFIG_DESCRIPTION = "If states is set, only groups in these states" +
                " will be returned. Otherwise, all groups are returned." +
                " This operation is supported by brokers with version 2.6.0 or later";
        private final Configuration configuration;

        public Config(final Configuration configuration) {
            this.configuration = configuration;
        }

        public boolean describeOffsets() {
            return ConfigProperty
                    .ofBoolean(OFFSETS_CONFIG_NAME)
                    .description(OFFSETS_DESCRIPTION)
                    .orElse(false)
                    .get(configuration);
        }

        public Set<ConsumerGroupState> inStates() {
            return ConfigProperty
                    .ofList(IN_STATE_CONFIG_NAME)
                    .description(IN_STATE_CONFIG_DESCRIPTION)
                    .getOptional(configuration)
                    .map(Config::toConsumerGroupStateSet)
                    .orElse(Collections.emptySet());
        }

        @NotNull
        private static Set<ConsumerGroupState> toConsumerGroupStateSet(Collection<String> states) {
            return states
                    .stream()
                    .map(name -> NAME_TO_ENUM.get(name.toUpperCase(Locale.ROOT)))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
    }
}
