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
package io.streamthoughts.jikkou.client.command;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import picocli.CommandLine;

public class SetOptionsMixin {

    @CommandLine.Option(names = { "--set-label", "-s" },
            description = "Set labels on the command line (can specify multiple values: -s key1=val1 -s key2=val2)"
    )
    public Map<String, Object> clientLabels = new HashMap<>();


    @CommandLine.Option(names = { "--set-value", "-v" },
            description = "Set variables on the command line to pass into the template engine built-in object 'Values' (can specify multiple values: -v key1=val1 -v key2=val2)"
    )
    public Map<String, Object> clientValues = new HashMap<>();

    @CommandLine.Option(
            names = {"--values-files"},
            arity = "1..*",
            description = "Specify the values-files containing the variables to pass into the template engine built-in object 'Values' (can specify multiple)."
    )
    public List<String> valuesFiles = new LinkedList<>();
}
