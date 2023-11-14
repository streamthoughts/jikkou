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

import io.streamthoughts.jikkou.client.command.BaseCommand;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.extension.Example;
import io.streamthoughts.jikkou.core.models.ApiExtension;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "get",
        header = "Print the API extension details.",
        description = "Print detailed information about the use of a specific API extension."
)
@Singleton
public class GetExtensionCommand extends BaseCommand implements Callable<Integer> {

    public static final String NOT_AVAILABLE = "N/A";
    @Parameters(
            paramLabel = "NAME",
            description = "Name of the extension.")
    private String name;

    @Inject
    private JikkouApi api;

    public GetExtensionCommand() {
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Integer call() {
        ApiExtensionList apiExtensions = api.getApiExtensions();

        Optional<ApiExtension> optional = apiExtensions.extensions()
                .stream()
                .filter(it -> it.name().equalsIgnoreCase(name))
                .findFirst();
        if (optional.isEmpty()) {
            System.err.printf("Unknown ApiExtension for name '%s'.%n", name);
            return CommandLine.ExitCode.SOFTWARE;
        }

        ApiExtension extension = optional.get();
        StringBuilder sb = new StringBuilder()
                .append("\n")
                .append("TITLE\n")
                .append(Optional.ofNullable(extension.title()).orElse(NOT_AVAILABLE))
                .append("\n\n")
                .append("DESCRIPTION\n")
                .append(Optional.ofNullable(extension.description()).orElse(NOT_AVAILABLE))
                .append("\n\n")
                .append("EXAMPLES");

        List<Example> examples = extension.examples();
        if (examples.isEmpty()) {
            sb.append("\n\n");
            sb.append(NOT_AVAILABLE);
        } else {
            for (Example example : examples) {
                sb.append("\n\n");
                sb.append(example.title());
                sb.append("\n\n");
                sb.append(String.join("\n", example.code()));
            }
        }
        System.out.println(sb);
        return CommandLine.ExitCode.OK;
    }
}
