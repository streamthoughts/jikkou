/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.aiven.reconciler;

import static io.jikkou.core.ReconciliationMode.CREATE;
import static io.jikkou.core.ReconciliationMode.DELETE;
import static io.jikkou.core.ReconciliationMode.FULL;

import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.exceptions.ConfigException;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.ChangeExecutor;
import io.jikkou.core.reconciler.ChangeHandler;
import io.jikkou.core.reconciler.ChangeResult;
import io.jikkou.core.reconciler.Controller;
import io.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.jikkou.core.selector.Selectors;
import io.jikkou.extension.aiven.AivenExtensionProvider;
import io.jikkou.extension.aiven.ApiVersions;
import io.jikkou.extension.aiven.api.AivenApiClient;
import io.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.jikkou.extension.aiven.change.acl.KafkaAclEntryChangeComputer;
import io.jikkou.extension.aiven.change.acl.KafkaAclEntryChangeHandler;
import io.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

@Title("Reconcile Aiven Kafka topic ACLs")
@Description("Reconciles Kafka topic ACL entry resources on an Aiven service to match the desired state.")
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, FULL}
)
@SupportedResource(type = V1KafkaTopicAclEntry.class)
@SupportedResource(
        apiVersion = ApiVersions.KAFKA_AIVEN_V1BETA1,
        kind = "KafkaTopicAclEntryChange"
)
public class AivenKafkaTopicAclEntryController implements Controller<V1KafkaTopicAclEntry> {

    interface Config {
        ConfigProperty<Boolean> DELETE_ORPHANS_OPTIONS = ConfigProperty
            .ofBoolean("delete-orphans")
            .defaultValue(false);
    }

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private AivenApiClientConfig apiClientConfig;
    private AivenKafkaTopicAclEntryCollector collector;

    /**
     * Creates a new {@link AivenKafkaTopicAclEntryController} instance.
     */
    public AivenKafkaTopicAclEntryController() {
    }

    /**
     * Creates a new {@link AivenKafkaTopicAclEntryController} instance.
     *
     * @param apiClientConfig the schema registry client configuration.
     */
    public AivenKafkaTopicAclEntryController(@NotNull AivenApiClientConfig apiClientConfig) {
        init(apiClientConfig);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull final ExtensionContext context) {
        init(context.<AivenExtensionProvider>provider().apiClientConfig());
    }

    private void init(@NotNull AivenApiClientConfig config) throws ConfigException {
        if (initialized.compareAndSet(false, true)) {
            this.apiClientConfig = config;
            this.collector = new AivenKafkaTopicAclEntryCollector(config);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> execute(@NotNull final ChangeExecutor executor,
                                      @NotNull ReconciliationContext context) {

        AivenApiClient api = AivenApiClientFactory.create(apiClientConfig);
        try {
            List<ChangeHandler> handlers = List.of(
                    new KafkaAclEntryChangeHandler.Create(api),
                    new KafkaAclEntryChangeHandler.Delete(api),
                    new KafkaAclEntryChangeHandler.None()
            );
            return executor.applyChanges(handlers);
        } finally {
            api.close();
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ResourceChange> plan(
            @NotNull Collection<V1KafkaTopicAclEntry> resources,
            @NotNull ReconciliationContext context) {

        // Get existing resources from the environment.
        List<V1KafkaTopicAclEntry> actualResources = collector.listAll(context.configuration(), Selectors.NO_SELECTOR).stream()
                .filter(context.selector()::apply)
                .toList();

        // Get expected resources which are candidates for this reconciliation.
        List<V1KafkaTopicAclEntry> expectedResources = resources.stream()
                .filter(context.selector()::apply)
                .toList();

        Boolean deleteOrphans = Config.DELETE_ORPHANS_OPTIONS.get(context.configuration());
        KafkaAclEntryChangeComputer computer = new KafkaAclEntryChangeComputer(deleteOrphans);

        return computer.computeChanges(actualResources, expectedResources);
    }
}
