/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.streamthoughts.jikkou.common.utils.Enums;
import io.streamthoughts.jikkou.core.data.avro.AvroSchema;
import io.streamthoughts.jikkou.core.data.json.Json;
import org.jetbrains.annotations.Nullable;

public enum SchemaType {

    /**
     * Avro Data Format
     */
    AVRO {
        @Override
        public Object comparableSchemaForm(final String schema, boolean useCanonicalFingerPrint) {
            if (schema == null) return null;

            return useCanonicalFingerPrint ? new AvroSchema(schema).fingerprint64() : Json.normalize(schema);
        }
    },
    /**
     * Protobug Data Format
     */
    PROTOBUF {
        @Override
        public Object comparableSchemaForm(final String schema, boolean useCanonicalFingerPrint) {
            return schema;
        }
    },
    /**
     * Json Data Format
     */
    JSON {
        @Override
        public Object comparableSchemaForm(final String schema, boolean useCanonicalFingerPrint) {
            return Json.normalize(schema);
        }
    };

    @JsonCreator
    public static SchemaType getForNameIgnoreCase(final @Nullable String str) {
        if (str == null) return AVRO;
        return Enums.getForNameIgnoreCase(str, SchemaType.class);
    }

    public static SchemaType defaultType() {
        return SchemaType.AVRO;
    }


    /**
     * Transforms the given schema to an object that will be used to check schema equality.
     *
     * @param schema                  The schema.
     * @param useCanonicalFingerPrint flag whether to use a canonical-print.
     * @return the object used to check schema equality.
     */
    public abstract Object comparableSchemaForm(final String schema,
                                                final boolean useCanonicalFingerPrint);

}
