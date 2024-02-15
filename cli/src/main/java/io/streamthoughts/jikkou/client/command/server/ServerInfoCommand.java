/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.server;

import io.micronaut.context.annotation.Requires;
import io.streamthoughts.jikkou.client.beans.ProxyConfiguration;
import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.http.client.JikkouApiClient;
import io.streamthoughts.jikkou.rest.data.Info;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "server-info",
        header = "Display Jikkou API server information",
        description = "Print information about the Jikkou API server"
)
@Singleton
@Requires(bean = ProxyConfiguration.class)
public class ServerInfoCommand extends CLIBaseCommand implements Callable<Integer> {

    enum Formats {JSON, YAML}

    @CommandLine.Option(names = {"--output", "-o"},
            defaultValue = "YAML",
            description = "Prints the output in the specified format. Allowed values: json, yaml (default yaml)."
    )
    Formats format;

    @Inject
    private JikkouApiClient apiClient;

    /**
     * {@inheritDoc}
     **/
    @Override
    public Integer call() throws Exception {
        Info serverInfo = apiClient.getServerInfo();
        if (serverInfo != null) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                switch (format) {
                    case JSON -> Jackson.JSON_OBJECT_MAPPER.writeValue(baos, serverInfo);
                    case YAML -> Jackson.YAML_OBJECT_MAPPER.writeValue(baos, serverInfo);
                }
                System.out.println(baos);
            }
        }
        return CommandLine.ExitCode.OK;
    }
}
