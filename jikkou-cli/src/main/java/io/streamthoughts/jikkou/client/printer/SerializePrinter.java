/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.client.printer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.error.JikkouException;
import java.util.Collection;

public class SerializePrinter implements Printer {

    private final ObjectMapper mapper;

    public SerializePrinter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /** {@inheritDoc} **/
    @Override
    public int print(Collection<ChangeResult<?>> results, boolean dryRun, long executionTimeMs) {
        final String json;
        try {
            json = mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(results);
            System.out.print(json);
        } catch (JsonProcessingException e) {
            throw new JikkouException(e);
        }
        return Printer.getNumberOfFailedChange(results) > 0 ? 1 : 0;
    }


}
