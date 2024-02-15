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
import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.core.models.ApiExtensionSummary;
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

    @Inject
    private JikkouApi api;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void run() {

        ApiExtensionList apiExtensions = Strings.isBlank(kind) ?
                api.getApiExtensions() : api.getApiExtensions(kind);

        Predicate<ApiExtensionSummary> predicate = Stream.<Predicate<ApiExtensionSummary>>of(
                ext -> category == null || ext.category().equalsIgnoreCase(category),
                ext -> provider == null || ext.provider().equalsIgnoreCase(category)
        ).reduce(Predicate::and).get();

        Stream<ApiExtensionSummary> extensions = apiExtensions.extensions()
                .stream()
                .filter(predicate)
                .sorted(Comparator.comparing(ApiExtensionSummary::name));

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
