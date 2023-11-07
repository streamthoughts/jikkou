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
package io.streamthoughts.jikkou.client.command.health;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import io.streamthoughts.jikkou.client.command.BaseCommand;
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
public class GetHealthIndicatorsCommand extends BaseCommand implements Runnable {

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
