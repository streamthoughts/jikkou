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
import io.jikkou.client.command.CLIBaseCommand;
import io.jikkou.client.command.OutputFormat;
import io.jikkou.client.command.OutputFormatMixin;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.models.ApiProviderList;
import io.jikkou.core.models.ApiProviderSummary;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "list",
        header = "Print the registered API providers",
        description = "Print the registered API providers")
@Singleton
public class ListProviderCommand extends CLIBaseCommand implements Callable<Integer> {

    @Mixin
    OutputFormatMixin outputFormat;

    @Inject
    private JikkouApi api;

    /** {@inheritDoc} **/
    @Override
    public Integer call() throws IOException {
        ApiProviderList apiProviders = api.getApiProviders();

        Stream<ApiProviderSummary> providers = apiProviders.providers()
                .stream()
                .sorted(Comparator.comparing(ApiProviderSummary::name));

        if (outputFormat.format() == OutputFormat.TABLE) {
            String[][] data = providers
                    .map(provider -> new String[]{
                            provider.name(),
                            provider.type(),
                            String.valueOf(provider.enabled())
                    })
                    .toArray(String[][]::new);

            String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                    new Column[]{
                            new Column().header("NAME").dataAlign(HorizontalAlign.LEFT),
                            new Column().header("TYPE").dataAlign(HorizontalAlign.LEFT),
                            new Column().header("ENABLED").dataAlign(HorizontalAlign.LEFT)
                    },
                    data);
            System.out.println(table);
        } else {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                outputFormat.format().serialize(providers.toList(), os);
                System.out.println(os);
            }
        }
        return CommandLine.ExitCode.OK;
    }
}
