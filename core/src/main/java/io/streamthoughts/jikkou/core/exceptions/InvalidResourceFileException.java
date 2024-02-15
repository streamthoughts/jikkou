/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.exceptions;

import java.net.URI;
import org.jetbrains.annotations.Nullable;

/**
 * Throws when a resource file cannot be read successfully.
 */
public class InvalidResourceFileException extends JikkouRuntimeException {

    private final URI location;

    /**
     * Creates a new {@link InvalidResourceFileException} instance.
     *
     * @param location   URI identifying the resource or null if not known.
     * @param cause    a cause message
     */
    public InvalidResourceFileException(@Nullable final URI location, final String cause) {
        super(cause);
        this.location = location;
    }

    /**
     * Returns the resource URI used to create this exception.
     *
     * @return  the URI (can be {@code null})
     */
    public URI getLocation() {
        return location;
    }
}
