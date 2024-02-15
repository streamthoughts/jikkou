/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io.writer;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.models.Resource;
import java.io.OutputStream;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Default interface to write resource.
 */
@Evolving
public interface ResourceWriter {

    enum Format {
        JSON, YAML
    }

    /**
     * Writes a resource using the specified resource.
     *
     * @param format   The output format
     * @param resource The Resource.
     * @param os       the output stream to write to
     */
    void write(@NotNull final Format format,
               @NotNull final Resource resource,
               @NotNull final OutputStream os);

    /**
     * Writes a resource using the specified resources.
     *
     * @param format the output format.
     * @param items  the list of {@link Resource} to write.
     * @param os     the output stream to write to
     */
    void write(@NotNull final Format format,
               @NotNull final List<? extends Resource> items,
               @NotNull final OutputStream os);
}
