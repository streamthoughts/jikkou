/*
 * Copyright 2021 The original authors
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
import io.streamthoughts.jikkou.api.JikkouContext;
import io.streamthoughts.jikkou.api.extensions.ExtensionDescriptor;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.api.health.HealthIndicator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Collection;
import picocli.CommandLine.Command;

@Command(name = "get-indicators",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Get all health indicators.",
        description = "Get all health indicators.",
        mixinStandardHelpOptions = true)
@Singleton
public class GetHealthIndicatorsCommand implements Runnable {

    @Inject
    private JikkouContext context;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {

        ExtensionFactory factory = context.getExtensionFactory();
        Collection<ExtensionDescriptor<HealthIndicator>> descriptors = factory
                .getAllDescriptorsForType(HealthIndicator.class);

        String[][] data = descriptors
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
