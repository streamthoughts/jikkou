/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.config;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import io.streamthoughts.jikkou.client.context.Context;
import io.streamthoughts.jikkou.runtime.JikkouConfig;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "view",
        description = "Show merged jikkou config settings"
)
@Singleton
public class ViewCommand extends CLIBaseCommand implements Runnable {

    @Option(names = "--name",
            required = false,
            description = "The name of configuration to view.")
    public String name;

    @Option(names = "--debug",
            required = false,
            defaultValue = "false",
            description = "Print configuration with the origin of setting as comments.")
    public boolean debug;

    @Option(names = "--comments",
            required = false,
            defaultValue = "false",
            description = "Print configuration with human-written comments.")
    public boolean comments;

    @Inject
    private ConfigurationContext configurationContext;

    /** {@inheritDoc} **/
    @Override
    public void run() {
        ConfigRenderOptions options = ConfigRenderOptions
                .defaults()
                .setOriginComments(debug)
                .setComments(comments);

        String configName = name != null ? name : configurationContext.getCurrentContextName();
        Context context = configurationContext.getContext(configName);

        JikkouConfig configuration = context.load();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Config unwrapped = configuration.unwrap();
            baos.write(unwrapped.root().render(options).getBytes(StandardCharsets.UTF_8));
            System.out.println(baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
