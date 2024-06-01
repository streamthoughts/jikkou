/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.avro;

import org.apache.avro.Schema;
import org.apache.avro.SchemaNormalization;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to wrap an Avro schema.
 *
 */
public final class AvroSchema {

    private final Schema schema;

    /**
     * Creates a new {@link AvroSchema} instance.
     *
     * @param schema    the avro schema string.
     */
    public AvroSchema(@NotNull String schema) {
        this.schema = new Schema.Parser().parse(schema);
    }

    public Schema schema() {
        return schema;
    }

    public long fingerprint64() {
        return SchemaNormalization.parsingFingerprint64(schema());
    }

    /** {@inheritDoc} **/
    public String toString() {
        return schema.toString(false);
    }
}
