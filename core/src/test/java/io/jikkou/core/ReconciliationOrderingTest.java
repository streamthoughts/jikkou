/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core;

import io.jikkou.core.annotation.ApiVersion;
import io.jikkou.core.annotation.Kind;
import io.jikkou.core.annotation.ReconciliationOrder;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.HasPriority;
import io.jikkou.core.models.ResourceType;
import io.jikkou.core.resource.DefaultResourceRegistry;
import io.jikkou.core.resource.ResourceDescriptor;
import io.jikkou.core.resource.ResourceDescriptorFactory;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReconciliationOrderingTest {

    private DefaultResourceRegistry registry;
    private ResourceDescriptorFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ResourceDescriptorFactory();
        registry = new DefaultResourceRegistry();
        registry.register(factory.make(ResourceType.of(NamespaceResource.class), NamespaceResource.class));
        registry.register(factory.make(ResourceType.of(TableResource.class), TableResource.class));
        registry.register(factory.make(ResourceType.of(ViewResource.class), ViewResource.class));
        registry.register(factory.make(ResourceType.of(UnorderedResourceA.class), UnorderedResourceA.class));
        registry.register(factory.make(ResourceType.of(UnorderedResourceB.class), UnorderedResourceB.class));
    }

    private int getReconciliationOrder(ResourceType type) {
        return registry.findDescriptorByType(type)
                .map(ResourceDescriptor::reconciliationOrder)
                .orElse(HasPriority.NO_ORDER);
    }

    @Test
    void shouldSortResourceTypesInAscendingOrderForCreation() {
        List<ResourceType> types = List.of(
                ResourceType.of(ViewResource.class),
                ResourceType.of(NamespaceResource.class),
                ResourceType.of(TableResource.class)
        );

        Comparator<ResourceType> comparator = Comparator
                .comparingInt((ResourceType t) -> getReconciliationOrder(t))
                .thenComparing(ResourceType::kind);

        List<String> sorted = types.stream().sorted(comparator).map(ResourceType::kind).toList();

        Assertions.assertEquals(List.of("Namespace", "Table", "View"), sorted);
    }

    @Test
    void shouldSortResourceTypesInDescendingOrderForDeletion() {
        List<ResourceType> types = List.of(
                ResourceType.of(NamespaceResource.class),
                ResourceType.of(ViewResource.class),
                ResourceType.of(TableResource.class)
        );

        Comparator<ResourceType> comparator = Comparator
                .comparingInt((ResourceType t) -> getReconciliationOrder(t))
                .thenComparing(ResourceType::kind)
                .reversed();

        List<String> sorted = types.stream().sorted(comparator).map(ResourceType::kind).toList();

        Assertions.assertEquals(List.of("View", "Table", "Namespace"), sorted);
    }

    @Test
    void shouldUseTiebreakerAlphabeticalByKindWhenSameOrder() {
        List<ResourceType> types = List.of(
                ResourceType.of(UnorderedResourceB.class),
                ResourceType.of(UnorderedResourceA.class)
        );

        Comparator<ResourceType> comparator = Comparator
                .comparingInt((ResourceType t) -> getReconciliationOrder(t))
                .thenComparing(ResourceType::kind);

        List<String> sorted = types.stream().sorted(comparator).map(ResourceType::kind).toList();

        // Both have NO_ORDER (0), so alphabetical tiebreaker: A before B
        Assertions.assertEquals(List.of("AlphaResource", "BetaResource"), sorted);
    }

    @Test
    void shouldSortUnorderedResourcesBeforeOrderedOnes() {
        List<ResourceType> types = List.of(
                ResourceType.of(UnorderedResourceA.class),
                ResourceType.of(NamespaceResource.class)
        );

        Comparator<ResourceType> comparator = Comparator
                .comparingInt((ResourceType t) -> getReconciliationOrder(t))
                .thenComparing(ResourceType::kind);

        List<String> sorted = types.stream().sorted(comparator).map(ResourceType::kind).toList();

        // UnorderedResourceA has NO_ORDER (0) which is less than 100, so it comes first
        Assertions.assertEquals(List.of("AlphaResource", "Namespace"), sorted);
    }

    @ApiVersion("test.jikkou.io/v1")
    @Kind("Namespace")
    @ReconciliationOrder(100)
    static abstract class NamespaceResource implements HasMetadata {}

    @ApiVersion("test.jikkou.io/v1")
    @Kind("Table")
    @ReconciliationOrder(200)
    static abstract class TableResource implements HasMetadata {}

    @ApiVersion("test.jikkou.io/v1")
    @Kind("View")
    @ReconciliationOrder(300)
    static abstract class ViewResource implements HasMetadata {}

    @ApiVersion("test.jikkou.io/v1")
    @Kind("AlphaResource")
    static abstract class UnorderedResourceA implements HasMetadata {}

    @ApiVersion("test.jikkou.io/v1")
    @Kind("BetaResource")
    static abstract class UnorderedResourceB implements HasMetadata {}
}
