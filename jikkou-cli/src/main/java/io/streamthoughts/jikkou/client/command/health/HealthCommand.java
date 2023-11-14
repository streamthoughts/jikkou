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

import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import picocli.CommandLine.Command;

@Command(name = "health",
        header = "Print or describe health indicators.",
        description = "This command can be used to describe all resources of a given kind",
        subcommands = {
                GetHealthCommand.class,
                GetHealthIndicatorsCommand.class
        })
public class HealthCommand extends CLIBaseCommand {
}
