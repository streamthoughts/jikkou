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

import io.streamthoughts.jikkou.core.change.Change;
import io.streamthoughts.jikkou.core.change.ChangeResult;
import io.streamthoughts.jikkou.core.io.Jackson;
import java.util.List;
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
    public int print(List<ChangeResult<Change>> results, boolean dryRun, long executionTimeInMillis) {
        return printer.print(results, dryRun, executionTimeInMillis);
    }
}
