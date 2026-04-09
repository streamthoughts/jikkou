/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command.extension;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import com.github.freva.asciitable.OverflowBehaviour;
import io.jikkou.client.command.CLIBaseCommand;
import io.jikkou.client.command.OutputFormat;
import io.jikkou.client.command.OutputFormatMixin;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.extension.Example;
import io.jikkou.core.models.ApiExtension;
import io.jikkou.core.models.ApiExtensionSpec;
import io.jikkou.core.models.ApiOptionSpec;
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
import picocli.CommandLine.Mixin;
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

    @Mixin
    OutputFormatMixin outputFormat;

    @Parameters(
            paramLabel = "NAME",
            description = "Name of the extension.")
    private String name;

    @Inject
    private JikkouApi api;

    public GetExtensionCommand() {
    }

    /** {@inheritDoc} **/
    @Override
    public Integer call() throws IOException {
        ApiExtension extension = api.getApiExtension(name);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            if (outputFormat.format() == OutputFormat.TABLE) {
                writeText(os, extension.spec());
            } else {
                outputFormat.format().serialize(extension, os);
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
