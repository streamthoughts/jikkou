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
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.core.config.Configuration;
import java.util.HashMap;
import java.util.Map;
import picocli.CommandLine.Option;

public final class ConfigOptionsMixin {

    @Option(
            names = {"--options"},
            description = "Set the configuration options to be used for computing resource reconciliation (can specify multiple values)"
    )
    public Map<String, Object> options = new HashMap<>();

    public Configuration getConfiguration() {
        return Configuration.from(options);
    }
}
