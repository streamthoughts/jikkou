/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.extension;

import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.action.Action;
import io.jikkou.core.action.ExecutionResultSet;
import io.jikkou.core.annotation.Enabled;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.converter.Converter;
import io.jikkou.core.health.Health;
import io.jikkou.core.health.HealthIndicator;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.models.ResourceType;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.ChangeExecutor;
import io.jikkou.core.reconciler.ChangeResult;
import io.jikkou.core.reconciler.Collector;
import io.jikkou.core.reconciler.Controller;
import io.jikkou.core.selector.Selector;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnabledAnnotationOnExtensionInterfacesTest {

    @Test
    void shouldDetectEnabledOnActionInterface() {
        Assertions.assertTrue(
                DefaultExtensionDescriptorFactory.isEnabled(TestAction.class));
    }

    @Test
    void shouldDetectEnabledOnConverterInterface() {
        Assertions.assertTrue(
                DefaultExtensionDescriptorFactory.isEnabled(TestConverter.class));
    }

    @Test
    void shouldDetectEnabledOnControllerInterface() {
        Assertions.assertTrue(
                DefaultExtensionDescriptorFactory.isEnabled(TestController.class));
    }

    @Test
    void shouldDetectEnabledOnCollectorInterface() {
        Assertions.assertTrue(
                DefaultExtensionDescriptorFactory.isEnabled(TestCollector.class));
    }

    @Test
    void shouldDetectEnabledOnHealthIndicatorInterface() {
        Assertions.assertTrue(
                DefaultExtensionDescriptorFactory.isEnabled(TestHealthIndicator.class));
    }

    @Test
    void shouldNotDetectEnabledWhenExtensionHasNoEnabledAnnotation() {
        Assertions.assertFalse(
                DefaultExtensionDescriptorFactory.isEnabled(PlainExtension.class));
    }

    @Test
    void shouldHaveEnabledAnnotationOnActionInterface() {
        Assertions.assertTrue(Action.class.isAnnotationPresent(Enabled.class));
    }

    @Test
    void shouldHaveEnabledAnnotationOnConverterInterface() {
        Assertions.assertTrue(Converter.class.isAnnotationPresent(Enabled.class));
    }

    // -- test stubs

    static class TestAction implements Action<HasMetadata> {
        @Override
        public boolean canAccept(@NotNull ResourceType type) {
            return false;
        }

        @Override
        public @NotNull ExecutionResultSet<HasMetadata> execute(@NotNull Configuration configuration) {
            return null;
        }
    }

    static class TestConverter implements Converter<HasMetadata, HasMetadata> {
        @Override
        public boolean canAccept(@NotNull ResourceType type) {
            return false;
        }

        @Override
        public @NotNull List<HasMetadata> apply(@NotNull HasMetadata resource) {
            return List.of();
        }
    }

    static class TestController implements Controller<HasMetadata> {
        @Override
        public boolean canAccept(@NotNull ResourceType type) {
            return false;
        }

        @Override
        public List<ResourceChange> plan(@NotNull Collection<HasMetadata> resources,
                                         @NotNull ReconciliationContext context) {
            return List.of();
        }

        @Override
        public List<ChangeResult> execute(@NotNull ChangeExecutor executor,
                                          @NotNull ReconciliationContext context) {
            return List.of();
        }
    }

    static class TestCollector implements Collector<HasMetadata> {
        @Override
        public boolean canAccept(@NotNull ResourceType type) {
            return false;
        }

        @Override
        public ResourceList<HasMetadata> listAll(@NotNull Configuration configuration,
                                                 @NotNull Selector selector) {
            return null;
        }
    }

    static class TestHealthIndicator implements HealthIndicator {
        @Override
        public Health getHealth(Duration timeout) {
            return null;
        }
    }

    static class PlainExtension implements Extension {
    }
}
