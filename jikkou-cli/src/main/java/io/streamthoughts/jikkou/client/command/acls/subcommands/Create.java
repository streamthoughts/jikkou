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
package io.streamthoughts.jikkou.client.command.acls.subcommands;

import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.client.command.acls.AclsCommand;
import picocli.CommandLine.Command;

@Command(name = "create",
        description = "Create the ACL policies missing on the cluster as describe in the specification file."
)
public class Create extends AclsCommand.Base {

    /** {@inheritDoc} */
    @Override
    public ReconciliationMode getReconciliationMode() {
        return ReconciliationMode.CREATE_ONLY;
    }
}
