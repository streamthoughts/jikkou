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
package io.streamthoughts.kafka.specs.io;

import io.streamthoughts.kafka.specs.error.JikkouException;
import io.streamthoughts.kafka.specs.model.V1SpecFile;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;

/**
 * Default interface to read a cluster specification.
 */
public interface SpecFileReader {

    /**
     * Retrieves a {@link V1SpecFile} from the specified input stream.
     *
     * @param specs    the input stream from which to read the specification.
     * @param vars     the vars passed through the command-line arguments.
     * @param labels   the labels passed through the command-line arguments.
     * @return         a new {@link V1SpecFile} instance.
     */
    V1SpecFile read(@NotNull final InputStream specs,
                    @NotNull final Map<String, Object> vars,
                    @NotNull final Map<String, Object> labels) throws JikkouException;
}
