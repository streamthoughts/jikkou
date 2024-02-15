/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.printer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;

public class SerializePrinter implements Printer {

    private final ObjectMapper mapper;

    public SerializePrinter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int print(ApiChangeResultList result, long executionTimeMs) {
        final String json;
        try {
            json = mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result);
            System.out.print(json);
        } catch (JsonProcessingException e) {
            throw new JikkouRuntimeException(e);
        }
        return Printer.getNumberOfFailedChange(result.results()) > 0 ? 1 : 0;
    }


}
