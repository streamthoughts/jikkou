/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.reconciler;

import static io.jikkou.core.ReconciliationMode.CREATE;
import static io.jikkou.core.ReconciliationMode.DELETE;
import static io.jikkou.core.ReconciliationMode.FULL;

import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.annotation.SupportedResource;
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
import io.jikkou.extension.confluent.ApiVersions;
import io.jikkou.extension.confluent.ConfluentCloudExtensionProvider;
import io.jikkou.extension.confluent.api.ConfluentCloudApiClient;
import io.jikkou.extension.confluent.api.ConfluentCloudApiClientConfig;
import io.jikkou.extension.confluent.api.ConfluentCloudApiClientFactory;
import io.jikkou.extension.confluent.change.RoleBindingChangeComputer;
import io.jikkou.extension.confluent.change.RoleBindingChangeHandler;
import io.jikkou.extension.confluent.models.V1RoleBinding;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

@ControllerConfiguration(
    supportedModes = {CREATE, DELETE, FULL}
)
@SupportedResource(type = V1RoleBinding.class)
@SupportedResource(
    apiVersion = ApiVersions.IAM_CONFLUENT_CLOUD_V1,
    kind = "RoleBindingChange"
)
public class ConfluentCloudRoleBindingController implements Controller<V1RoleBinding> {

    interface Config {
        ConfigProperty<Boolean> DELETE_ORPHANS_OPTIONS = ConfigProperty
            .ofBoolean("delete-orphans")
            .displayName("Delete Orphan Role Bindings")
            .description("Specifies whether to delete role bindings that are not defined in the resource definitions")
            .defaultValue(false);
    }

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private ConfluentCloudApiClientConfig apiClientConfig;
    private ConfluentCloudRoleBindingCollector collector;

    public ConfluentCloudRoleBindingController() {
    }

    public ConfluentCloudRoleBindingController(@NotNull ConfluentCloudApiClientConfig apiClientConfig) {
        init(apiClientConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        init(context.<ConfluentCloudExtensionProvider>provider().apiClientConfig());
    }

    private void init(@NotNull ConfluentCloudApiClientConfig config) throws ConfigException {
        if (initialized.compareAndSet(false, true)) {
            this.apiClientConfig = config;
            this.collector = new ConfluentCloudRoleBindingCollector(config);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResult> execute(@NotNull final ChangeExecutor executor,
                                      @NotNull ReconciliationContext context) {

        ConfluentCloudApiClient api = ConfluentCloudApiClientFactory.create(apiClientConfig);
        try {
            List<ChangeHandler> handlers = List.of(
                new RoleBindingChangeHandler.Create(api),
                new RoleBindingChangeHandler.Delete(api),
                new RoleBindingChangeHandler.None()
            );
            return executor.applyChanges(handlers);
        } finally {
            api.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceChange> plan(
        @NotNull Collection<V1RoleBinding> resources,
        @NotNull ReconciliationContext context) {

        // Get existing resources from the environment.
        List<V1RoleBinding> actualResources = collector.listAll(context.configuration(), Selectors.NO_SELECTOR).stream()
            .filter(context.selector()::apply)
            .toList();

        // Get expected resources which are candidates for this reconciliation.
        List<V1RoleBinding> expectedResources = resources.stream()
            .filter(context.selector()::apply)
            .toList();

        Boolean deleteOrphans = Config.DELETE_ORPHANS_OPTIONS.get(context.configuration());
        RoleBindingChangeComputer computer = new RoleBindingChangeComputer(deleteOrphans);

        return computer.computeChanges(actualResources, expectedResources);
    }
}
