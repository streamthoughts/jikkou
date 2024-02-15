/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.health;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicatorList;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import picocli.CommandLine.Command;

@Command(name = "get-indicators",
        header = "Get all health indicators.",
        description = "Get all health indicators."
)
@Singleton
public class GetHealthIndicatorsCommand extends CLIBaseCommand implements Runnable {

    @Inject
    private JikkouApi api;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {

        ApiHealthIndicatorList indicatorList = api.getApiHealthIndicators();

        String[][] data = indicatorList
                .indicators()
                .stream()
                .map(descriptor -> new String[]{
                        descriptor.name(),
                        descriptor.description()
                })
                .toArray(String[][]::new);

        String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                new Column[]{
                        new Column().header("NAME").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("DESCRIPTION").dataAlign(HorizontalAlign.LEFT),
                },
                data);
        System.out.println(table);
    }
}
