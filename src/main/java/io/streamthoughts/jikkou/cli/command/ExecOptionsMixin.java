/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.cli.command;

import picocli.CommandLine.Option;

import java.util.regex.Pattern;

public class ExecOptionsMixin {

    @Option(names = "--verbose",
            description = "Print resources details."
    )
    public boolean verbose;

    @Option(names = "--yes",
            defaultValue = "false",
            description = "Assume yes; assume that the answer to any question which would be asked is yes.",
            arity = "0..1")
    public Boolean yes;

    @Option(names = "--dry-run",
            description = "Execute command in Dry-Run mode."
    )
    public boolean dryRun;

    @Option(names = "--exclude",
            description = "The regex patterns to use for excluding resources.")
    public Pattern[] exclude;

    @Option(names = "--include",
            description = "The regex patterns to use for including resources.")
    public Pattern[] include;
}