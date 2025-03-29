/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io.reader;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.NamedValueSet;

/**
 * Service interface for reading values files.
 */
public interface ValuesReader extends AutoCloseable {

    /**
     * Reads all the values.
     */
    NamedValueSet readAll(ValuesReaderOptions options) throws JikkouRuntimeException;

    /**
     * {@inheritDoc}
     */
    @Override
    default void close() {
    }
}
