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

import io.streamthoughts.jikkou.client.command.BaseCommand;
import jakarta.inject.Singleton;
import picocli.CommandLine.Command;

@Command(name = "config",
        subcommands = {
                ViewCommand.class,
                SetContextCommand.class,
                GetContextsCommand.class,
                CurrentContextCommand.class,
                UseContextCommand.class},
        description = "Sets or retrieves the configuration of this client"

)
@Singleton
public class ConfigCommand extends BaseCommand { }
