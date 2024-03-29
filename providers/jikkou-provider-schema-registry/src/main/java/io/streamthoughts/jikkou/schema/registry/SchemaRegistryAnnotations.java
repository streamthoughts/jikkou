/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry;/*
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

import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.NamedValue;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;

public final class SchemaRegistryAnnotations {

    private static final String SCHEMAREGISTRY_JIKKOU_IO = "schemaregistry.jikkou.io/";
    public static final String JIKKOU_IO_SCHEMA_REGISTRY_NORMALIZE_SCHEMA = SCHEMAREGISTRY_JIKKOU_IO + "normalize-schema";
    public static final String JIKKOU_IO_SCHEMA_REGISTRY_PERMANANTE_DELETE = SCHEMAREGISTRY_JIKKOU_IO + "permanent-delete";
    public static final String JIKKOU_IO_SCHEMA_REGISTRY_URL = SCHEMAREGISTRY_JIKKOU_IO + "url";
    public static final String JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_VERSION = SCHEMAREGISTRY_JIKKOU_IO + "schema-version";
    public static final String JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_ID = SCHEMAREGISTRY_JIKKOU_IO + "schema-id";

    public static boolean isAnnotatedWithNormalizeSchema(V1SchemaRegistrySubject subject) {
        return CoreAnnotations.isAnnotatedWith(subject, JIKKOU_IO_SCHEMA_REGISTRY_NORMALIZE_SCHEMA);
    }

    public static boolean isAnnotatedWitPermananteDelete(V1SchemaRegistrySubject subject) {
        return CoreAnnotations.isAnnotatedWith(subject, JIKKOU_IO_SCHEMA_REGISTRY_PERMANANTE_DELETE);
    }

    public static String schemaId(V1SchemaRegistrySubject subject) {
        return HasMetadata.getMetadataAnnotation(subject, JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_ID)
                .map(NamedValue::getValue)
                .map(o -> TypeConverter.String().convertValue(o))
                .orElse("");
    }

    public static String version(V1SchemaRegistrySubject subject) {
        return HasMetadata.getMetadataAnnotation(subject, JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_VERSION)
                .map(NamedValue::getValue)
                .map(o -> TypeConverter.String().convertValue(o))
                .orElse("");
    }
}
