/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.jikkou.api.error;

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
