/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io.reader;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Default interface to read a cluster specification.
 */
@InterfaceStability.Evolving
public interface ResourceReader extends AutoCloseable {

    /**
     * Reads all the resources.
     */
    List<HasMetadata> readAllResources(@NotNull ResourceReaderOptions options) throws JikkouRuntimeException;

    /**
     * {@inheritDoc}
     */
    @Override
    default void close() { }
}
