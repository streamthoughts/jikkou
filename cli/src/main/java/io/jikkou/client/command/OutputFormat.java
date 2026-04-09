/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command;

import io.jikkou.core.io.Jackson;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Output formats supported by CLI commands that display non-resource data
 * (providers, extensions, resources).
 */
public enum OutputFormat {
    TABLE,
    JSON,
    YAML;

    /**
     * Serializes the given object to the output stream using this format.
     * Not applicable for {@link #TABLE} — callers must handle table rendering themselves.
     *
     * @param value the object to serialize.
     * @param os    the output stream.
     * @throws IOException              if serialization fails.
     * @throws UnsupportedOperationException if called on {@link #TABLE}.
     */
    public void serialize(Object value, OutputStream os) throws IOException {
        switch (this) {
            case JSON -> Jackson.JSON_OBJECT_MAPPER
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(os, value);
            case YAML -> Jackson.YAML_OBJECT_MAPPER
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(os, value);
            case TABLE -> throw new UnsupportedOperationException(
                    "TABLE format must be handled by the command itself.");
        }
    }
}
