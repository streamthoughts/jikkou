/*
 * Copyright 2022 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.ReconciliationChangeResultList;
import io.streamthoughts.jikkou.core.reconcilier.Change;

public class SerializePrinter implements Printer {

    private final ObjectMapper mapper;

    public SerializePrinter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /** {@inheritDoc} **/
    @Override
    public int print(ReconciliationChangeResultList<Change> result, long executionTimeMs) {
        final String json;
        try {
            json = mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result);
            System.out.print(json);
        } catch (JsonProcessingException e) {
            throw new JikkouRuntimeException(e);
        }
        return Printer.getNumberOfFailedChange(result.getChanges()) > 0 ? 1 : 0;
    }


}
