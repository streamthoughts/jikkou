/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Names;
import io.streamthoughts.jikkou.core.annotation.ReconciliationOrder;
import io.streamthoughts.jikkou.core.annotation.Verbs;
import io.streamthoughts.jikkou.core.models.HasPriority;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.Verb;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.jetbrains.annotations.NotNull;

/**
 * Factory to create new {@link ResourceDescriptor} instance.
 */
public final class ResourceDescriptorFactory {

    /**
     * Makes a new {@link ResourceDescriptor} instance.
     *
     * @param type     the type of the resource. Cannot be {@code null}.
     * @param resource the class of the resource. Cannot be {@code null}.
     * @return a new instance of {@link ResourceDescriptor}.
     * @throws NullPointerException if either type or resource is {@code  null}.
     */
    public ResourceDescriptor make(@NotNull final ResourceType type,
                                   @NotNull final Class<? extends Resource> resource) {
        Objects.requireNonNull(type, "Cannot make ResourceDescriptor for type 'null'");
        Objects.requireNonNull(type, "Cannot make ResourceDescriptor for resource 'null'");
        String description = extractDescription(resource);

        Names names = resource.getAnnotation(Names.class);

        ResourceDescriptor descriptor;
        if (names != null) {
            descriptor = new ResourceDescriptor(
                    type,
                    description,
                    resource,
                    Strings.isNullOrEmpty(names.singular()) ? null : names.singular(),
                    Strings.isNullOrEmpty(names.plural()) ? null : names.plural(),
                    new TreeSet<>(Arrays.asList(names.shortNames())),
                    extractVerbs(resource),
                    Resource.isTransient(resource)
            );
        } else {
            descriptor = new ResourceDescriptor(
                    type,
                    description,
                    resource,
                    null,
                    null,
                    Collections.emptySet(),
                    extractVerbs(resource),
                    Resource.isTransient(resource)
            );
        }
        descriptor.setReconciliationOrder(extractReconciliationOrder(resource));
        return descriptor;
    }

    private static int extractReconciliationOrder(@NotNull Class<? extends Resource> resource) {
        return Optional.ofNullable(resource.getAnnotation(ReconciliationOrder.class))
                .map(ReconciliationOrder::value)
                .orElse(HasPriority.NO_ORDER);
    }

    @NotNull
    private static Set<Verb> extractVerbs(@NotNull Class<? extends Resource> resource) {
        Verb[] verbs = Optional
                .ofNullable(resource.getAnnotation(Verbs.class))
                .map(Verbs::value).orElse(new Verb[]{});
        return new HashSet<>(Arrays.asList(verbs));
    }

    @NotNull
    private static String extractDescription(@NotNull Class<? extends Resource> resource) {
        return Optional.ofNullable(resource.getAnnotation(Description.class))
                .map(Description::value)
                .orElse("");
    }
}
