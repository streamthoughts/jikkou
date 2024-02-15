/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import io.streamthoughts.jikkou.client.context.Context;
import io.streamthoughts.jikkou.core.io.Jackson;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import picocli.CommandLine.Command;

@Command(
        name = "current-context",
        description = "Displays the current context"
)
@Singleton
public class CurrentContextCommand extends CLIBaseCommand implements Runnable {

    @Inject
    private ConfigurationContext configurationContext;

    @Override
    public void run() {
        Context currentContext = configurationContext.getCurrentContext();
        String[][] data = new String[2][];
        data[0] = new String[]{ "ConfigFile", currentContext.configFile() };
        data[1] = new String[]{ "ConfigProps", getString(currentContext)};
        String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                new Column[]{
                        new Column().header("KEY").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("VALUE").dataAlign(HorizontalAlign.LEFT)
                },
                data);

        System.out.println("Using context '" + configurationContext.getCurrentContextName() + "'");
        System.out.println();
        System.out.println(table);
    }

    private static String getString(Context currentContext) {
        try {
            return Jackson.JSON_OBJECT_MAPPER.writeValueAsString(currentContext.configProps());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
