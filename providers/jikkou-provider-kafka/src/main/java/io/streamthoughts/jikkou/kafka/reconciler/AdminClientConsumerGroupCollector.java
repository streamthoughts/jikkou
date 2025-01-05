/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;
import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaAdminService;
import java.util.List;
import java.util.Set;
import org.apache.kafka.common.ConsumerGroupState;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1KafkaConsumerGroup.class)
public final class AdminClientConsumerGroupCollector extends ContextualExtension implements Collector<V1KafkaConsumerGroup> {

    /**
     * The extension config.
     */
    public interface Config {
        ConfigProperty<Boolean> OFFSETS = ConfigProperty
            .ofBoolean("offsets")
            .description("Specify whether consumer group offsets should be described.")
            .defaultValue(false);

        ConfigProperty<Set<ConsumerGroupState>> IN_STATES = ConfigProperty
            .ofAny("in-states")
            .convert(TypeConverter.ofSet(ConsumerGroupState.class))
            .description(
                "If states is set, only groups in these states will be returned. Otherwise, all groups are returned." +
                    " This operation is supported by brokers with version 2.6.0 or later")
            .defaultValue(Set.of());
    }


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
            KafkaAdminService service = new KafkaAdminService(clientContext.getAdminClient());
            return service.listConsumerGroups(Config.IN_STATES.get(configuration), Config.OFFSETS.get(configuration));
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(
            Config.IN_STATES,
            Config.OFFSETS
        );
    }
}
