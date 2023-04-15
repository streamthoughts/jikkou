/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.client.command.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import io.streamthoughts.jikkou.api.io.Jackson;
import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import io.streamthoughts.jikkou.client.context.Context;
import picocli.CommandLine;

@CommandLine.Command(name = "current-context", description = "Displays the current context")
public class CurrentContextCommand implements Runnable {

    ConfigurationContext context = new ConfigurationContext();

    @Override
    public void run() {
        Context currentContext = context.getCurrentContext();
        String[][] data = new String[2][];
        data[0] = new String[]{ "ConfigFile", currentContext.configFile() };
        data[1] = new String[]{ "ConfigProps", getString(currentContext)};
        String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                new Column[]{
                        new Column().header("KEY").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("VALUE").dataAlign(HorizontalAlign.LEFT)
                },
                data);

        System.out.println("Using context '" + context.getCurrentContextName() + "'");
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
