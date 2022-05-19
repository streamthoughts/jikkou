/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.api.extensions;

import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

public class ExternalExtension {

    private final URL location;
    private final URL[] resources;

    /**
     * Creates a new {@link ExternalExtension} instance.
     *
     * @param location  the component top-level location.
     * @param resources the component resources.
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
        if (!(o instanceof ExternalExtension)) return false;
        ExternalExtension that = (ExternalExtension) o;
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
        return "ExternalExtension{" +
                "location=" + location +
                ", resources=" + Arrays.toString(resources) +
                '}';
    }
}
