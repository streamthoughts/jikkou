/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension.qualifier;

import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.Qualifier;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.Arrays;

public final class Qualifiers {

    @SafeVarargs
    public static <T> Qualifier<T> byAnyQualifiers(final Qualifier<T>... qualifiers) {
        return new AnyQualifier<>(Arrays.asList(qualifiers));
    }

    @SafeVarargs
    public static <T> Qualifier<T> byQualifiers(final Qualifier<T>... qualifiers) {
        return new CompositeQualifier<>(Arrays.asList(qualifiers));
    }

    public static<T> Qualifier<T> byName(final String name) {
        return new NamedQualifier<>(name);
    }
    public static<T> Qualifier<T> bySupportedResource(final ResourceType type) {
        return new SupportedResourceQualifier<>(type);
    }

    public static<T> Qualifier<T> byCategory(final ExtensionCategory category) {
        return new CategoryQualifier<>(category);
    }

    public static <T> Qualifier<T> enabled() {
        return new EnabledQualifier<>(true);
    }

}
