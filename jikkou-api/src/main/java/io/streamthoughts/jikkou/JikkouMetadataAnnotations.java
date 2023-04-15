/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou;

import io.streamthoughts.jikkou.api.model.HasMetadata;

public final class JikkouMetadataAnnotations {

    public static final String JIKKOU_IO_IGNORE = "jikkou.io/ignore";
    public static final String JIKKOU_IO_DELETE = "jikkou.io/delete";

    private JikkouMetadataAnnotations() {
    }

    public static boolean isAnnotatedWithIgnore(final HasMetadata resource) {
        return HasMetadata.getMetadataAnnotation(resource, JikkouMetadataAnnotations.JIKKOU_IO_IGNORE)
                .map(Object::toString)
                .map(Boolean::parseBoolean)
                .orElse(false);
    }

    public static boolean isAnnotatedWithDelete(final HasMetadata resource) {
        return HasMetadata.getMetadataAnnotation(resource, JikkouMetadataAnnotations.JIKKOU_IO_DELETE)
                .map(Object::toString)
                .map(Boolean::parseBoolean)
                .orElse(false);
    }
}
