/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.extension;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.client.command.OutputFormat;
import io.streamthoughts.jikkou.client.command.OutputFormatMixin;
import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.core.models.ApiExtensionSummary;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Stream;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "list",
        header = "Print the supported API extensions",
        description = "Print the supported API extensions")
@Singleton
public class ListExtensionCommand extends CLIBaseCommand implements Callable<Integer> {

    @Option(names = {"--category"},
            description = "Limit to extensions of the specified category."
    )
    public String category;

    @Option(names = {"--provider"},
            description = "Limit to extensions of the specified provider."
    )
    public String provider;

    @Option(names = {"--kind"},
            description = "Limit to extensions that support the specified resource kind."
    )
    public String kind;

    @Mixin
    OutputFormatMixin outputFormat;

    @Inject
    private JikkouApi api;

    /** {@inheritDoc} **/
    @Override
    public Integer call() throws IOException {

        ApiExtensionList apiExtensions = Strings.isNullOrEmpty(kind) ?
                api.getApiExtensions() : api.getApiExtensions(kind);

        Predicate<ApiExtensionSummary> predicate = Stream.<Predicate<ApiExtensionSummary>>of(
                ext -> category == null || ext.category().equalsIgnoreCase(category),
                ext -> provider == null || ext.provider().equalsIgnoreCase(provider)
        ).reduce(Predicate::and).get();

        Stream<ApiExtensionSummary> extensions = apiExtensions.extensions()
                .stream()
                .filter(predicate)
                .sorted(Comparator.comparing(ApiExtensionSummary::provider)
                        .thenComparing(ApiExtensionSummary::category)
                        .thenComparing(ApiExtensionSummary::name));

        if (outputFormat.format() == OutputFormat.TABLE) {
            String[][] data = extensions
                    .map(extension -> new String[]{
                            extension.provider(),
                            extension.category(),
                            extension.name(),
                            Objects.toString(extension.title(), ""),
                            String.valueOf(extension.enabled())
                    })
                    .toArray(String[][]::new);

            String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                    new Column[]{
                            new Column().header("PROVIDER").dataAlign(HorizontalAlign.LEFT),
                            new Column().header("CATEGORY").dataAlign(HorizontalAlign.LEFT),
                            new Column().header("NAME").dataAlign(HorizontalAlign.LEFT),
                            new Column().header("TITLE").dataAlign(HorizontalAlign.LEFT),
                            new Column().header("ENABLED").dataAlign(HorizontalAlign.LEFT)
                    },
                    data);
            System.out.println(table);
        } else {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                outputFormat.format().serialize(extensions.toList(), os);
                System.out.println(os);
            }
        }
        return CommandLine.ExitCode.OK;
    }
}
