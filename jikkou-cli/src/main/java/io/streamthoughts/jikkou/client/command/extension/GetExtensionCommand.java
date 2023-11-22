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
package io.streamthoughts.jikkou.client.command.extension;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import com.github.freva.asciitable.OverflowBehaviour;
import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.extension.Example;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.ApiExtension;
import io.streamthoughts.jikkou.core.models.ApiExtensionSpec;
import io.streamthoughts.jikkou.core.models.ApiOptionSpec;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "get",
        header = "Print the API extension details.",
        description = "Print detailed information about the use of a specific API extension."
)
@Singleton
public class GetExtensionCommand extends CLIBaseCommand implements Callable<Integer> {

    private static final String NOT_AVAILABLE = "N/A";
    private static final String NEW_LINE = System.lineSeparator();
    private static final String WIDE_COLUMN_TITLE = "TITLE";
    private static final String WIDE_COLUMN_DESCRIPTION = "DESCRIPTION";
    private static final String WIDE_COLUMN_OPTIONS = "OPTIONS";
    private static final String WIDE_COLUMN_EXAMPLES = "EXAMPLES";

    enum Format {
        JSON, YAML, WIDE
    }

    @Option(names = {"--output", "-o"},
            defaultValue = "WIDE",
            description = "Prints the output in the specified format. Valid values: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})."
    )

    private Format format;

    @Parameters(
            paramLabel = "NAME",
            description = "Name of the extension.")
    private String name;

    @Inject
    private JikkouApi api;

    public GetExtensionCommand() {
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Integer call() throws IOException {
        ApiExtension extension = api.getApiExtension(name);

        ApiExtensionSpec spec = extension.spec();
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            switch (format) {
                case JSON -> Jackson.JSON_OBJECT_MAPPER
                        .writerWithDefaultPrettyPrinter()
                        .writeValue(os, extension);
                case YAML -> Jackson.YAML_OBJECT_MAPPER
                        .writerWithDefaultPrettyPrinter()
                        .writeValue(os, extension);
                case WIDE -> writeText(os, spec);
            }
            System.out.println(os);
            return CommandLine.ExitCode.OK;
        }

    }

    private static void writeText(OutputStream os, ApiExtensionSpec spec) {
        StringBuilder sb = new StringBuilder()
                .append(NEW_LINE)
                .append(WIDE_COLUMN_TITLE)
                .append(NEW_LINE)
                .append(Optional.ofNullable(spec.title()).orElse(NOT_AVAILABLE))
                .append(NEW_LINE)
                .append(NEW_LINE)
                .append(WIDE_COLUMN_DESCRIPTION)
                .append(NEW_LINE)
                .append(Optional.ofNullable(spec.description()).orElse(NOT_AVAILABLE))
                .append(NEW_LINE)
                .append(NEW_LINE)
                .append(WIDE_COLUMN_OPTIONS)
                .append(NEW_LINE)
                .append(writeOptionsAsTable(spec.options()))
                .append(NEW_LINE)
                .append(NEW_LINE)
                .append(WIDE_COLUMN_EXAMPLES);

        List<Example> examples = spec.examples();
        if (examples.isEmpty()) {
            sb.append(NEW_LINE);
            sb.append(NOT_AVAILABLE);
        } else {
            for (Example example : examples) {
                sb.append(NEW_LINE);
                sb.append(NEW_LINE);
                sb.append(example.title());
                sb.append(NEW_LINE);
                sb.append(NEW_LINE);
                sb.append(String.join(NEW_LINE, example.code()));
            }
        }
        try (var writer = new PrintWriter(os)) {
            writer.write(sb.toString());
        }
    }

    private static String writeOptionsAsTable(List<ApiOptionSpec> optionSpecs) {
        List<String[]> options = new ArrayList<>();
        for (ApiOptionSpec option : optionSpecs) {
            String[] strings = new String[]{
                    option.name(),
                    option.description(),
                    option.type(),
                    String.valueOf(option.required())
            };
            options.add(strings);
        }
        String[][] data = options.toArray(new String[0][]);
        return AsciiTable.getTable(AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS,
                new Column[]{
                        new Column().header("NAME").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(20, OverflowBehaviour.NEWLINE),
                        new Column().header(WIDE_COLUMN_DESCRIPTION).dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(80, OverflowBehaviour.NEWLINE),
                        new Column().header("TYPE").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(10, OverflowBehaviour.NEWLINE),
                        new Column().header("REQUIRED").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(10, OverflowBehaviour.NEWLINE)
                },
                data
        );
    }
}
