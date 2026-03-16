/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.provider;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.models.ApiProviderList;
import io.streamthoughts.jikkou.core.models.ApiProviderSummary;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Comparator;
import java.util.stream.Stream;
import picocli.CommandLine.Command;

@Command(name = "list",
        header = "Print the registered API providers",
        description = "Print the registered API providers")
@Singleton
public class ListProviderCommand extends CLIBaseCommand implements Runnable {

    @Inject
    private JikkouApi api;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void run() {
        ApiProviderList apiProviders = api.getApiProviders();

        Stream<ApiProviderSummary> providers = apiProviders.providers()
                .stream()
                .sorted(Comparator.comparing(ApiProviderSummary::name));

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
    }
}
