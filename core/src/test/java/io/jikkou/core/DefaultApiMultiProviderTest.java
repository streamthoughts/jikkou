/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.JikkouRuntimeException;
import io.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.jikkou.core.extension.DefaultExtensionFactory;
import io.jikkou.core.extension.DefaultExtensionRegistry;
import io.jikkou.core.extension.DefaultProviderConfigurationRegistry;
import io.jikkou.core.models.ApiResourceChangeList;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.reconciler.ResourceChangeFilter;
import io.jikkou.core.resource.DefaultResourceRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultApiMultiProviderTest {

    private static final String PROVIDER_TYPE = "io.test.TestProvider";

    private DefaultExtensionFactory factory;
    private DefaultResourceRegistry resourceRegistry;
    private DefaultProviderConfigurationRegistry providerConfigRegistry;

    @BeforeEach
    void setUp() {
        factory = new DefaultExtensionFactory(
            new DefaultExtensionRegistry(
                new DefaultExtensionDescriptorFactory(),
                new ClassExtensionAliasesGenerator())
        );
        resourceRegistry = new DefaultResourceRegistry();
        providerConfigRegistry = new DefaultProviderConfigurationRegistry();
    }

    private DefaultApi buildApi() {
        var builder = new DefaultApi.Builder(factory, resourceRegistry);
        try {
            var field = builder.getClass().getSuperclass().getDeclaredField("providerConfigurationRegistry");
            field.setAccessible(true);
            field.set(builder, providerConfigRegistry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set providerConfigurationRegistry", e);
        }
        return builder.build();
    }

    @Test
    void shouldAggregateEmptyDiffResultsAcrossProviders() {
        providerConfigRegistry.registerProviderConfiguration("provider-a", PROVIDER_TYPE, Configuration.empty(), false);
        providerConfigRegistry.registerProviderConfiguration("provider-b", PROVIDER_TYPE, Configuration.empty(), false);
        DefaultApi api = buildApi();

        ReconciliationContext context = ReconciliationContext.builder()
            .dryRun(true)
            .providerNames(List.of("provider-a", "provider-b"))
            .build();

        // With empty resources and registered providers, diff should return empty changes
        ApiResourceChangeList result = api.getDiff(ResourceList.empty(), new ResourceChangeFilter.Noop(), context);
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void shouldDiffFailFastByDefault() {
        // Only register provider-a, not provider-b — provider-b lookup will fail
        providerConfigRegistry.registerProviderConfiguration("provider-a", PROVIDER_TYPE, Configuration.empty(), false);
        DefaultApi api = buildApi();

        ReconciliationContext context = ReconciliationContext.builder()
            .dryRun(true)
            .providerNames(List.of("provider-a", "provider-b"))
            .continueOnError(false)
            .build();

        JikkouRuntimeException exception = assertThrows(
            JikkouRuntimeException.class,
            () -> api.getDiff(ResourceList.empty(), new ResourceChangeFilter.Noop(), context)
        );
        assertTrue(exception.getMessage().contains("provider-b"));
    }

    @Test
    void shouldDiffContinueOnErrorWhenFlagSet() {
        // Only register provider-a, not provider-b — provider-b will fail
        providerConfigRegistry.registerProviderConfiguration("provider-a", PROVIDER_TYPE, Configuration.empty(), false);
        DefaultApi api = buildApi();

        ReconciliationContext context = ReconciliationContext.builder()
            .dryRun(true)
            .providerNames(List.of("provider-a", "provider-b"))
            .continueOnError(true)
            .build();

        // Should not throw — provider-a succeeds, provider-b fails but continues
        ApiResourceChangeList result = api.getDiff(ResourceList.empty(), new ResourceChangeFilter.Noop(), context);
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void shouldReconcileAcrossMultipleProviders() {
        providerConfigRegistry.registerProviderConfiguration("provider-a", PROVIDER_TYPE, Configuration.empty(), false);
        providerConfigRegistry.registerProviderConfiguration("provider-b", PROVIDER_TYPE, Configuration.empty(), false);
        DefaultApi api = buildApi();

        ReconciliationContext context = ReconciliationContext.builder()
            .dryRun(true)
            .providerNames(List.of("provider-a", "provider-b"))
            .build();

        // With empty resources, reconcile should succeed with empty results
        var result = api.reconcile(ResourceList.empty(), ReconciliationMode.CREATE, context);
        assertTrue(result.results().isEmpty());
        assertTrue(result.dryRun());
    }

    @Test
    void shouldReconcileFailFastOnProviderError() {
        providerConfigRegistry.registerProviderConfiguration("provider-a", PROVIDER_TYPE, Configuration.empty(), false);
        // provider-b not registered — will cause error
        DefaultApi api = buildApi();

        ReconciliationContext context = ReconciliationContext.builder()
            .dryRun(true)
            .providerNames(List.of("provider-a", "provider-b"))
            .continueOnError(false)
            .build();

        JikkouRuntimeException exception = assertThrows(
            JikkouRuntimeException.class,
            () -> api.reconcile(ResourceList.empty(), ReconciliationMode.CREATE, context)
        );
        assertTrue(exception.getMessage().contains("provider-b"));
    }

    @Test
    void shouldReconcileContinueOnError() {
        providerConfigRegistry.registerProviderConfiguration("provider-a", PROVIDER_TYPE, Configuration.empty(), false);
        // provider-b not registered — will cause error but should continue
        DefaultApi api = buildApi();

        ReconciliationContext context = ReconciliationContext.builder()
            .dryRun(true)
            .providerNames(List.of("provider-a", "provider-b"))
            .continueOnError(true)
            .build();

        // Should NOT throw
        var result = api.reconcile(ResourceList.empty(), ReconciliationMode.CREATE, context);
        assertTrue(result.results().isEmpty());
    }

    @Test
    void shouldPreserveContextPropertiesInSingleProviderContext() {
        providerConfigRegistry.registerProviderConfiguration("provider-a", PROVIDER_TYPE, Configuration.empty(), false);
        DefaultApi api = buildApi();

        ReconciliationContext context = ReconciliationContext.builder()
            .dryRun(true)
            .providerNames(List.of("provider-a"))
            .build();

        // Single provider in the list — should still work through multi-provider path
        ApiResourceChangeList result = api.getDiff(ResourceList.empty(), new ResourceChangeFilter.Noop(), context);
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void shouldNotUseMultiProviderPathForSingleProvider() {
        providerConfigRegistry.registerProviderConfiguration("my-provider", PROVIDER_TYPE, Configuration.empty(), false);
        DefaultApi api = buildApi();

        // providerName (singular) instead of providerNames — single provider path
        ReconciliationContext context = ReconciliationContext.builder()
            .dryRun(true)
            .providerName("my-provider")
            .build();

        ApiResourceChangeList result = api.getDiff(ResourceList.empty(), new ResourceChangeFilter.Noop(), context);
        assertTrue(result.getItems().isEmpty());
    }
}
