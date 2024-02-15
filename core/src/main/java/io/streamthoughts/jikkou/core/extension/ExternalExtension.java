/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

/**
 * An ExternalExtension.
 */
public final class ExternalExtension {

    private final URL location;
    private final URL[] resources;

    /**
     * Creates a new {@link ExternalExtension} instance.
     *
     * @param location  The extension top-level location.
     * @param resources the extension resources.
     */
    ExternalExtension(final URL location, final URL[] resources) {
        this.location = location;
        this.resources = resources;
    }

    public URL location() {
        return location;
    }

    public URL[] resources() {
        return resources;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExternalExtension that)) return false;
        return Objects.equals(location, that.location) &&
                Arrays.equals(resources, that.resources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(location);
        result = 31 * result + Arrays.hashCode(resources);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ExternalExtension [" +
                "location=" + location +
                ", resources=" + Arrays.toString(resources) +
                ']';
    }
}
