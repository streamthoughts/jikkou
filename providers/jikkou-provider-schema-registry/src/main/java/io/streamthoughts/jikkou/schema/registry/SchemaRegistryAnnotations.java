/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry;

import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.NamedValue;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;

public final class SchemaRegistryAnnotations {

    private static final String SCHEMA_REGISTRY_JIKKOU_IO = "schemaregistry.jikkou.io/";
    public static final String SCHEMA_REGISTRY_NORMALIZE_SCHEMA = SCHEMA_REGISTRY_JIKKOU_IO + "normalize-schema";
    public static final String SCHEMA_REGISTRY_PERMANANTE_DELETE = SCHEMA_REGISTRY_JIKKOU_IO + "permanent-delete";
    public static final String SCHEMA_REGISTRY_URL = SCHEMA_REGISTRY_JIKKOU_IO + "url";
    public static final String SCHEMA_REGISTRY_SCHEMA_VERSION = SCHEMA_REGISTRY_JIKKOU_IO + "schema-version";
    public static final String SCHEMA_REGISTRY_SCHEMA_ID = SCHEMA_REGISTRY_JIKKOU_IO + "schema-id";
    public static final String SCHEMA_REGISTRY_USE_CANONICAL_FINGERPRINT = SCHEMA_REGISTRY_JIKKOU_IO + "use-canonical-fingerprint";

    private final V1SchemaRegistrySubject resource;

    public SchemaRegistryAnnotations(final V1SchemaRegistrySubject resource) {
        this.resource = resource;
    }

    public boolean normalizeSchema() {
        return CoreAnnotations.isAnnotatedWith(resource, SCHEMA_REGISTRY_NORMALIZE_SCHEMA);
    }

    public boolean permananteDelete() {
        return CoreAnnotations.isAnnotatedWith(resource, SCHEMA_REGISTRY_PERMANANTE_DELETE);
    }

    public boolean useCanonicalFingerPrint() {
        return CoreAnnotations.isAnnotatedWith(resource, SCHEMA_REGISTRY_USE_CANONICAL_FINGERPRINT);
    }

    public String schemaId() {
        return HasMetadata.getMetadataAnnotation(resource, SCHEMA_REGISTRY_SCHEMA_ID)
            .map(NamedValue::getValue)
            .map(o -> TypeConverter.String().convertValue(o))
            .orElse("");
    }

    public String version() {
        return HasMetadata.getMetadataAnnotation(resource, SCHEMA_REGISTRY_SCHEMA_VERSION)
            .map(NamedValue::getValue)
            .map(o -> TypeConverter.String().convertValue(o))
            .orElse("");
    }
}
