/*
 * Copyright 2020 The original authors
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
package io.streamthoughts.jikkou.core.io.writer;

import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.Resource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Default ResourceWriter implementation.
 */
public final class DefaultResourceWriter implements ResourceWriter {

    /**
     * Creates a new {@link DefaultResourceWriter} instance.
     */
    public DefaultResourceWriter() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(@NotNull Format format,
                      @NotNull Resource resource,
                      @NotNull OutputStream os) {
        try {
            switch (format) {
                case JSON:
                    writeJSON(os, resource);
                    break;
                case YAML:
                    writeYAML(os, resource);
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object into '" + format + "' format", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(@NotNull Format format,
                      @NotNull List<? extends Resource> items,
                      @NotNull OutputStream os) {
        try {
            switch (format) {
                case JSON:
                    writeJSON(os, items);
                    break;
                case YAML:
                    for (Object item : items) {
                        writeYAML(os, item);
                    }
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object into '" + format + "' format", e);
        }
    }

    private void writeJSON(@NotNull OutputStream os,
                           @NotNull Object object) throws IOException {
        Jackson.JSON_OBJECT_MAPPER.writeValue(os, object);
    }

    private void writeYAML(@NotNull OutputStream os,
                           @NotNull Object object) throws IOException {
        Jackson.YAML_OBJECT_MAPPER.writeValue(os, object);
    }
}
