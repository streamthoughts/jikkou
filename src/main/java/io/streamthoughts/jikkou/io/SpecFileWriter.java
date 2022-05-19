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
package io.streamthoughts.jikkou.io;

import io.streamthoughts.jikkou.api.model.V1SpecFile;

import java.io.OutputStream;

/**
 * Default interface to write a cluster specification.
 */
public interface SpecFileWriter {

    /**
     * Writes the cluster specification into the specified output stream.
     *
     * @param spec  the cluster specification.
     * @param os    the output stream.
     */
    void write(final V1SpecFile spec, final OutputStream os);
}
