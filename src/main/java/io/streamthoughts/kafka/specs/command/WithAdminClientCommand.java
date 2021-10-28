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
package io.streamthoughts.kafka.specs.command;

import io.streamthoughts.kafka.specs.Jikkou;
import io.streamthoughts.kafka.specs.internal.AdminClientUtils;
import org.apache.kafka.clients.admin.AdminClient;
import picocli.CommandLine.ParentCommand;

import java.util.Properties;
import java.util.function.Function;

public class WithAdminClientCommand {

    @ParentCommand
    private Jikkou specs;

    public Integer withAdminClient(final Function<AdminClient, Integer> function) {
        final Properties adminClientProps = specs.options.getConfig().getAdminClientProps();
        try (AdminClient client = AdminClientUtils.newAdminClient(adminClientProps)) {
            return function.apply(client);
        }
    }

}
