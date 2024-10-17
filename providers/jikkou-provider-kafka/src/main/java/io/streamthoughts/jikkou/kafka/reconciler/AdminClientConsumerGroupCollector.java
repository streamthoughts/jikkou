/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import static io.streamthoughts.jikkou.kafka.reconciler.AdminClientConsumerGroupCollector.IN_STATE_CONFIG_DESCRIPTION;
import static io.streamthoughts.jikkou.kafka.reconciler.AdminClientConsumerGroupCollector.IN_STATE_CONFIG_NAME;
import static io.streamthoughts.jikkou.kafka.reconciler.AdminClientConsumerGroupCollector.OFFSETS_CONFIG_DESCRIPTION;
import static io.streamthoughts.jikkou.kafka.reconciler.AdminClientConsumerGroupCollector.OFFSETS_CONFIG_NAME;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionOptionSpec;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionSpec;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;
import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaConsumerGroupService;
import java.util.Collections;
import java.util.Set;
import org.apache.kafka.common.ConsumerGroupState;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1KafkaConsumerGroup.class)
@ExtensionSpec(
    options = {
        @ExtensionOptionSpec(
            name = OFFSETS_CONFIG_NAME,
            description = OFFSETS_CONFIG_DESCRIPTION,
            type = Boolean.class,
            defaultValue = "false"
        ),
        @ExtensionOptionSpec(
            name = IN_STATE_CONFIG_NAME,
            description = IN_STATE_CONFIG_DESCRIPTION,
            type = Set.class
        )
    }
)
public final class AdminClientConsumerGroupCollector extends ContextualExtension implements Collector<V1KafkaConsumerGroup> {

    public static final String OFFSETS_CONFIG_NAME = "offsets";
    public static final String OFFSETS_CONFIG_DESCRIPTION = "Specify whether consumer group offsets should be described.";
    public static final String IN_STATE_CONFIG_NAME = "in-states";
    public static final String IN_STATE_CONFIG_DESCRIPTION = "If states is set, only groups in these states" +
        " will be returned. Otherwise, all groups are returned." +
        " This operation is supported by brokers with version 2.6.0 or later";

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
        super.init(context);
        if (adminClientContextFactory == null) {
            adminClientContextFactory = context.<KafkaExtensionProvider>provider().newAdminClientContextFactory();
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceList<V1KafkaConsumerGroup> listAll(@NotNull Configuration configuration,
                                                      @NotNull Selector selector) {
        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            KafkaConsumerGroupService service = new KafkaConsumerGroupService(clientContext.getAdminClient());

            boolean describeOffsets = extensionContext()
                .<Boolean>configProperty(OFFSETS_CONFIG_NAME)
                .get(configuration);

            Set<ConsumerGroupState> inStates = ConfigProperty
                .of(IN_STATE_CONFIG_NAME, TypeConverter.ofSet(ConsumerGroupState.class))
                .getOptional(configuration)
                .orElse(Collections.emptySet());

            return service.listConsumerGroups(
                inStates,
                describeOffsets
            );
        }
    }
}
