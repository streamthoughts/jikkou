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
package io.streamthoughts.jikkou.core.models;

import org.jetbrains.annotations.NotNull;

/**
 * Core Metadata Annotations.
 */
public final class CoreAnnotations {

    /**
     * The jikkou.io/ prefix is reserved for use by Jikkou core components.
     */
    public static final String PREFIX = "jikkou.io/";

    public static final String JKKOU_IO_MANAGED_BY_LOCATION = PREFIX + "managed-by-location";
    public static final String JKKOU_IO_RESOURCE_GENERATED = PREFIX + "generated";
    public static final String JIKKOU_IO_ITEMS_COUNT = PREFIX + "items-count";
    public static final String JIKKOU_IO_CHANGE_COUNT = PREFIX + "changes-count";
    public static final String JIKKOU_IO_IGNORE = PREFIX + "ignore";
    public static final String JIKKOU_IO_DELETE = PREFIX + "delete";
    public static final String JIKKOU_BYPASS_VALIDATIONS = PREFIX + "bypass-validations";
    public static final String JIKKOU_NO_REPORT = PREFIX + "no-report";
    public static final String JIKKOU_IO_TRANSFORM_PREFIX = "transform.jikkou.io";

    private CoreAnnotations() {}

    public static boolean isAnnotatedWithNoReport(final HasMetadata resource) {
        return isAnnotatedWith(resource, CoreAnnotations.JIKKOU_NO_REPORT);
    }

    public static boolean isAnnotatedWithByPassValidation(final HasMetadata resource) {
        return isAnnotatedWith(resource, CoreAnnotations.JIKKOU_BYPASS_VALIDATIONS);
    }

    public static boolean isAnnotatedWithIgnore(final HasMetadata resource) {
        return isAnnotatedWith(resource, CoreAnnotations.JIKKOU_IO_IGNORE);
    }

    public static boolean isAnnotatedWithDelete(final HasMetadata resource) {
        return isAnnotatedWith(resource, CoreAnnotations.JIKKOU_IO_DELETE);
    }

    @NotNull
    public static Boolean isAnnotatedWith(@NotNull HasMetadata resource, @NotNull String annotation) {
        return HasMetadata.getMetadataAnnotation(resource, annotation)
                .map(Object::toString)
                .map(Boolean::parseBoolean)
                .orElse(false);
    }
}
