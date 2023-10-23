/*
 * Copyright 2023 The original authors
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
package io.streamthoughts.jikkou.client.command.validate;

import static io.streamthoughts.jikkou.client.printer.Ansi.isColor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import com.github.freva.asciitable.OverflowBehaviour;
import io.streamthoughts.jikkou.client.printer.Ansi;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.resource.validation.ValidationError;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class ValidationErrorsWriter {

    private static final String TABLE_COLUMN_NAME = "NAME";
    private static final String TABLE_COLUMN_ERROR = "ERROR";
    private static final String TABLE_COLUMN_DETAILS = "DETAILS";

    public static @NotNull OutputStream write(@NotNull List<ValidationError> errors) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            List<String[]> list = new ArrayList<>();
            for (int i = 0; i < errors.size(); i++) {
                ValidationError error = errors.get(i);
                String[] strings = new String[]{
                        "#" + (i+1),
                        error.name(),
                        error.message(),
                        getDetailsAsString(error.details())

                };
                list.add(strings);
            }
            String[][] data = list.toArray(new String[0][]);

            String table = AsciiTable.getTable(AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS,
                    new Column[]{
                            new Column(),
                            new Column().header(TABLE_COLUMN_NAME).dataAlign(HorizontalAlign.LEFT)
                                    .maxWidth(30, OverflowBehaviour.NEWLINE),
                            new Column().header(TABLE_COLUMN_ERROR).dataAlign(HorizontalAlign.LEFT)
                                    .maxWidth(60, OverflowBehaviour.NEWLINE),
                            new Column().header(TABLE_COLUMN_DETAILS).dataAlign(HorizontalAlign.LEFT)
                                    .maxWidth(45, OverflowBehaviour.NEWLINE),
                    },
                    data);
            baos.write(String.format(
                    "%sError: %sInvalid resources%n%n%s",
                    isColor() ? Ansi.Color.RED : "",
                    isColor() ? Ansi.Color.BOLD_WHITE : "",
                    isColor() ? Ansi.Color.DEFAULT :  ""
            ).getBytes(StandardCharsets.UTF_8));
            baos.write(table.getBytes(StandardCharsets.UTF_8));
            return baos;
        }
    }

    private static String getDetailsAsString(Map<String, Object> values) {
        try {
            return Jackson.JSON_OBJECT_MAPPER.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
