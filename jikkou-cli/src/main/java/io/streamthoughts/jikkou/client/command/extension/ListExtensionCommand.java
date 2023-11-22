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
package io.streamthoughts.jikkou.client.command.extension;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.models.ApiExtension;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "list",
        header = "Print the supported API extensions",
        description = "Print the supported API extensions")
@Singleton
public class ListExtensionCommand extends CLIBaseCommand implements Runnable {

    @Option(names = {"--category"},
            required = false,
            description = "Limit to extensions of the specified category."
    )
    public String category;

    @Option(names = {"--provider"},
            required = false,
            description = "Limit to extensions of the specified provider."
    )
    public String provider;

    @Option(names = {"--kind"},
            required = false,
            description = "Limit to extensions that support the specified resource kind."
    )
    public String kind;

    @Inject
    private JikkouApi api;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void run() {

        ApiExtensionList apiExtensions = Strings.isBlank(kind) ?
                api.getApiExtensions() : api.getApiExtensions(kind);

        Predicate<ApiExtension> predicate = Stream.<Predicate<ApiExtension>>of(
                ext -> category == null || ext.category().equalsIgnoreCase(category),
                ext -> provider == null || ext.provider().equalsIgnoreCase(category)
        ).reduce(Predicate::and).get();

        Stream<ApiExtension> extensions = apiExtensions.extensions()
                .stream()
                .filter(predicate)
                .sorted(Comparator.comparing(ApiExtension::name));

        String[][] data = extensions
                .map(extension -> new String[]{
                        extension.name(),
                        extension.provider(),
                        extension.category()
                })
                .toArray(String[][]::new);

        String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                new Column[]{
                        new Column().header("NAME").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("PROVIDER").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("CATEGORY").dataAlign(HorizontalAlign.LEFT)
                },
                data);
        System.out.println(table);
    }
}
