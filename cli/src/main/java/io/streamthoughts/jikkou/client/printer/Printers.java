/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.printer;

import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import org.jetbrains.annotations.NotNull;

public enum Printers implements Printer {

    TEXT(new TextPrinter(true)),
    COMPACT(new TextPrinter(false)),
    JSON(new SerializePrinter(Jackson.JSON_OBJECT_MAPPER)),
    YAML(new SerializePrinter(Jackson.YAML_OBJECT_MAPPER));

    private final Printer printer;

    Printers(@NotNull Printer printer) {
        this.printer = printer;
    }

    /** {@inheritDoc} **/
    @Override
    public int print(ApiChangeResultList result, long executionTimeInMillis) {
        return printer.print(result, executionTimeInMillis);
    }
}
