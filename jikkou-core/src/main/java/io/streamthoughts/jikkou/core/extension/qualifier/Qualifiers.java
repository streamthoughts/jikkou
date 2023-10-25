/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.core.extension.qualifier;

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
    public static<T> Qualifier<T> byAcceptedResource(final ResourceType type) {
        return new AcceptedResourceQualifier<>(type);
    }

    public static <T> Qualifier<T> enabled() {
        return new EnabledQualifier<>(true);
    }

}
