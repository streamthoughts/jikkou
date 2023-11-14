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
