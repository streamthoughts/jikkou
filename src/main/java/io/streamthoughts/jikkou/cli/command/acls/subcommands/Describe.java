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
package io.streamthoughts.jikkou.cli.command.acls.subcommands;

import io.streamthoughts.jikkou.api.config.JikkouConfig;
import io.streamthoughts.jikkou.api.config.JikkouParams;
import io.streamthoughts.jikkou.api.manager.AclDescribeOptions;
import io.streamthoughts.jikkou.api.manager.KafkaAclsManager;
import io.streamthoughts.jikkou.cli.command.BaseCommand;
import io.streamthoughts.jikkou.io.YAMLSpecWriter;
import io.streamthoughts.jikkou.api.model.MetaObject;
import io.streamthoughts.jikkou.api.model.V1AccessUserObject;
import io.streamthoughts.jikkou.api.model.V1SecurityObject;
import io.streamthoughts.jikkou.api.model.V1SpecFile;
import io.streamthoughts.jikkou.api.model.V1SpecObject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;

@Command(name = "describe",
        description = "Describe all the ACLs that currently exist on remote cluster."
)
public class Describe extends BaseCommand {

    @Option(names = "--file-path",
            description = "The file path to write the description of Topics."
    )
    File filePath;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {

        final KafkaAclsManager manager = JikkouParams.KAFKA_ACLS_MANAGER.get(JikkouConfig.get());

        final Collection<V1AccessUserObject> users = manager.describe(new AclDescribeOptions());

        try {
            OutputStream os = (filePath != null) ? new FileOutputStream(filePath) : System.out;
            final V1SpecObject specsObject = V1SpecObject.withSecurity(new V1SecurityObject(users, null));
            YAMLSpecWriter.instance().write(new V1SpecFile(MetaObject.defaults(), specsObject), os);
            return CommandLine.ExitCode.OK;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
