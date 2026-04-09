/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command.provider;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import com.github.freva.asciitable.OverflowBehaviour;
import io.jikkou.client.command.CLIBaseCommand;
import io.jikkou.client.command.OutputFormat;
import io.jikkou.client.command.OutputFormatMixin;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.models.ApiExtensionSummary;
import io.jikkou.core.models.ApiOptionSpec;
import io.jikkou.core.models.ApiProvider;
import io.jikkou.core.models.ApiProviderSpec;
import io.jikkou.core.models.ApiResourceSummary;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

@Command(name = "get",
        header = "Print the API provider details.",
        description = "Print detailed information about a specific API provider."
)
@Singleton
public class GetProviderCommand extends CLIBaseCommand implements Callable<Integer> {

    private static final String NOT_AVAILABLE = "N/A";
    private static final String NEW_LINE = System.lineSeparator();

    @Mixin
    OutputFormatMixin outputFormat;

    @Parameters(
            paramLabel = "NAME",
            description = "Name of the provider.")
    private String name;

    @Inject
    private JikkouApi api;

    public GetProviderCommand() {
    }

    /** {@inheritDoc} **/
    @Override
    public Integer call() throws IOException {
        ApiProvider provider = api.getApiProvider(name);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            if (outputFormat.format() == OutputFormat.TABLE) {
                writeText(os, provider.spec());
            } else {
                outputFormat.format().serialize(provider, os);
            }
            System.out.println(os);
            return CommandLine.ExitCode.OK;
        }
    }

    private static void writeText(OutputStream os, ApiProviderSpec spec) {
        StringBuilder sb = new StringBuilder()
                .append(NEW_LINE)
                .append("DESCRIPTION")
                .append(NEW_LINE)
                .append(Optional.ofNullable(spec.description()).filter(s -> !s.isEmpty()).orElse(NOT_AVAILABLE))
                .append(NEW_LINE)
                .append(NEW_LINE)
                .append("TAGS")
                .append(NEW_LINE)
                .append(spec.tags().isEmpty() ? NOT_AVAILABLE : String.join(", ", spec.tags()))
                .append(NEW_LINE)
                .append(NEW_LINE)
                .append("EXTERNAL DOCS")
                .append(NEW_LINE)
                .append(Optional.ofNullable(spec.externalDocs()).filter(s -> !s.isEmpty()).orElse(NOT_AVAILABLE))
                .append(NEW_LINE)
                .append(NEW_LINE)
                .append("OPTIONS")
                .append(NEW_LINE)
                .append(writeOptionsAsTable(spec.options()))
                .append(NEW_LINE)
                .append(NEW_LINE)
                .append("RESOURCES")
                .append(NEW_LINE)
                .append(writeResourcesAsTable(spec.resources()))
                .append(NEW_LINE)
                .append(NEW_LINE)
                .append("EXTENSIONS")
                .append(NEW_LINE)
                .append(writeExtensionsAsTable(spec.extensions()));

        try (var writer = new PrintWriter(os)) {
            writer.write(sb.toString());
        }
    }

    private static String writeOptionsAsTable(List<ApiOptionSpec> optionSpecs) {
        if (optionSpecs.isEmpty()) {
            return NOT_AVAILABLE;
        }
        List<String[]> options = new ArrayList<>();
        for (ApiOptionSpec option : optionSpecs) {
            String[] strings = new String[]{
                    Objects.toString(option.displayName(), ""),
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
                                .maxWidth(30, OverflowBehaviour.NEWLINE),
                        new Column().header("CONFIG KEY").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(40, OverflowBehaviour.NEWLINE),
                        new Column().header("DESCRIPTION").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(60, OverflowBehaviour.NEWLINE),
                        new Column().header("TYPE").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(10, OverflowBehaviour.NEWLINE),
                        new Column().header("REQUIRED").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(10, OverflowBehaviour.NEWLINE)
                },
                data
        );
    }

    private static String writeResourcesAsTable(List<ApiResourceSummary> resources) {
        if (resources.isEmpty()) {
            return NOT_AVAILABLE;
        }
        String[][] data = resources.stream()
                .map(res -> new String[]{
                        res.kind(),
                        res.apiVersion(),
                        Objects.toString(res.description(), "")
                })
                .toArray(String[][]::new);

        return AsciiTable.getTable(AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS,
                new Column[]{
                        new Column().header("KIND").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(40, OverflowBehaviour.NEWLINE),
                        new Column().header("API VERSION").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(40, OverflowBehaviour.NEWLINE),
                        new Column().header("DESCRIPTION").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(60, OverflowBehaviour.NEWLINE)
                },
                data
        );
    }

    private static String writeExtensionsAsTable(List<ApiExtensionSummary> extensions) {
        if (extensions.isEmpty()) {
            return NOT_AVAILABLE;
        }
        String[][] data = extensions.stream()
                .map(ext -> new String[]{
                        ext.name(),
                        Objects.toString(ext.title(), ""),
                        ext.category(),
                        String.valueOf(ext.enabled())
                })
                .toArray(String[][]::new);

        return AsciiTable.getTable(AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS,
                new Column[]{
                        new Column().header("NAME").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(60, OverflowBehaviour.NEWLINE),
                        new Column().header("TITLE").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(40, OverflowBehaviour.NEWLINE),
                        new Column().header("CATEGORY").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(30, OverflowBehaviour.NEWLINE),
                        new Column().header("ENABLED").dataAlign(HorizontalAlign.LEFT)
                                .maxWidth(10, OverflowBehaviour.NEWLINE)
                },
                data
        );
    }
}
