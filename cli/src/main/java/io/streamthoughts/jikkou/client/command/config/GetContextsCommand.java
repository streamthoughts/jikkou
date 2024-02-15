/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.config;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import io.streamthoughts.jikkou.client.context.Context;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import picocli.CommandLine.Command;

@Command(
        name = "get-contexts",
        description = "Get all contexts"
)
@Singleton
public class GetContextsCommand extends CLIBaseCommand implements Runnable {

    @Inject
    private ConfigurationContext configurationContext;

    @Override
    public void run() {
        Map<String, Context> contexts = configurationContext.getContexts();
        String current = configurationContext.getCurrentContextName();

        String[][] data = contexts.keySet()
                .stream()
                .map(context -> new String[]{context + (context.equals(current) ? "*" : "")})
                .toArray(String[][]::new);

        String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                new Column[]{
                        new Column().header("NAME").dataAlign(HorizontalAlign.LEFT),
                },
                data);

        System.out.println(table);
    }
}