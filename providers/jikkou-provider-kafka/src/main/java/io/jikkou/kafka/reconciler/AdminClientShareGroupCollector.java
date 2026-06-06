/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.reconciler;

import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.data.TypeConverter;
import io.jikkou.core.extension.ContextualExtension;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.reconciler.Collector;
import io.jikkou.core.selector.Selector;
import io.jikkou.kafka.KafkaExtensionProvider;
import io.jikkou.kafka.internals.admin.AdminClientContext;
import io.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.jikkou.kafka.models.V1KafkaShareGroup;
import io.jikkou.kafka.reconciler.service.KafkaAdminService;
import java.util.List;
import java.util.Set;
import org.apache.kafka.common.GroupState;
import org.jetbrains.annotations.NotNull;

@Title("Collect Kafka share groups")
@Description("Collects all Kafka share group resources from a Kafka cluster using the AdminClient API.")
@SupportedResource(type = V1KafkaShareGroup.class)
public final class AdminClientShareGroupCollector extends ContextualExtension implements Collector<V1KafkaShareGroup> {

    /**
     * The extension config.
     */
    public interface Config {
        ConfigProperty<Boolean> OFFSETS = ConfigProperty
            .ofBoolean("offsets")
            .displayName("Offsets")
            .description("Specify whether share group offsets (SPSO) should be described.")
            .defaultValue(false);

        ConfigProperty<Set<GroupState>> IN_STATES = ConfigProperty
            .ofAny("in-states")
            .displayName("In States")
            .convert(TypeConverter.ofSet(GroupState.class))
            .description("If set, only share groups in these states are returned. Otherwise, all groups are returned.")
            .defaultValue(Set.of());
    }

    private AdminClientContextFactory adminClientContextFactory;

    /**
     * Creates a new {@link AdminClientShareGroupCollector} instance.
     * CLI requires any empty constructor.
     */
    public AdminClientShareGroupCollector() {
        super();
    }

    /**
     * Creates a new {@link AdminClientShareGroupCollector} instance with the specified {@link AdminClientContextFactory}.
     *
     * @param adminClientContextFactory the {@link AdminClientContextFactory} to use.
     */
    public AdminClientShareGroupCollector(final @NotNull AdminClientContextFactory adminClientContextFactory) {
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
     */
    @Override
    public ResourceList<V1KafkaShareGroup> listAll(@NotNull Configuration configuration,
                                                   @NotNull Selector selector) {
        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            KafkaAdminService service = new KafkaAdminService(clientContext.getAdminClient());
            return service.listShareGroups(Config.IN_STATES.get(configuration), Config.OFFSETS.get(configuration));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(Config.IN_STATES, Config.OFFSETS);
    }
}
