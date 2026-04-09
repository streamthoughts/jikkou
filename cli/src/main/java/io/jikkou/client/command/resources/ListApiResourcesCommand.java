/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command.resources;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import io.jikkou.client.command.CLIBaseCommand;
import io.jikkou.client.command.OutputFormat;
import io.jikkou.client.command.OutputFormatMixin;
import io.jikkou.common.utils.Strings;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.models.ApiResource;
import io.jikkou.core.models.ApiResourceList;
import io.jikkou.core.models.Verb;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "list",
        header = "Print the supported API resources",
        description = "List the API resources supported by the Jikkou CLI or Jikkou API Server (in proxy mode)."
)
@Singleton
public class ListApiResourcesCommand extends CLIBaseCommand implements Callable<Integer> {

    private List<Verb> verbs;

    @Option(names = {"--api-group"},
            required = false,
            description = "Limit to resources in the specified API group."
    )
    public String group;

    @Option(names = {"--verbs"},
            required = false,
            description = "Limit to resources that support the specified verbs."
    )
    public void setVerbs(List<String> verbs) {
        this.verbs = verbs.stream().map(Verb::getForNameIgnoreCase).collect(Collectors.toList());
    }

    @Mixin
    OutputFormatMixin outputFormat;

    @Inject
    private JikkouApi api;

    /** {@inheritDoc} **/
    @Override
    public Integer call() throws IOException {
        List<ApiResourceList> apiResourceLists = Strings.isNullOrEmpty(group) ?
                api.listApiResources() :
                api.listApiResources(group);

        if (outputFormat.format() == OutputFormat.TABLE) {
            String[][] data = apiResourceLists
                    .stream()
                    .flatMap(apiResourceList -> {
                        List<ApiResource> resources = apiResourceList.resources();
                        return resources
                                .stream()
                                .filter(resource -> {
                                    if (verbs == null || verbs.isEmpty()) return true;
                                    return verbs.stream().allMatch(resource::isVerbSupported);
                                })
                                .map(resource -> new String[]{
                                        resource.name(),
                                        String.join(", ", resource.shortNames()),
                                        apiResourceList.groupVersion(),
                                        resource.kind(),
                                        String.join(", ", resource.verbs())
                                });
                    })
                    .toArray(String[][]::new);
            String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                    new Column[]{
                            new Column().header("NAME").dataAlign(HorizontalAlign.LEFT),
                            new Column().header("SHORTNAMES").dataAlign(HorizontalAlign.LEFT),
                            new Column().header("APIVERSION").dataAlign(HorizontalAlign.LEFT),
                            new Column().header("KIND").dataAlign(HorizontalAlign.LEFT),
                            new Column().header("VERBS").dataAlign(HorizontalAlign.LEFT)
                    },
                    data);
            System.out.println(table);
        } else {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                outputFormat.format().serialize(apiResourceLists, os);
                System.out.println(os);
            }
        }
        return CommandLine.ExitCode.OK;
    }
}
