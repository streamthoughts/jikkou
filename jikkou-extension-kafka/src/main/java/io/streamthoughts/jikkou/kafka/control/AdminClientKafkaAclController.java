/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.control;

import static io.streamthoughts.jikkou.api.ReconciliationMode.APPLY;
import static io.streamthoughts.jikkou.api.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.api.ReconciliationMode.DELETE;

import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.annotations.SupportedReconciliationModes;
import io.streamthoughts.jikkou.api.annotations.SupportedResource;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.BaseExternalResourceController;
import io.streamthoughts.jikkou.api.control.ChangeExecutor;
import io.streamthoughts.jikkou.api.control.ChangeHandler;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.selector.AggregateSelector;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
import io.streamthoughts.jikkou.kafka.control.change.AclChange;
import io.streamthoughts.jikkou.kafka.control.change.AclChangeComputer;
import io.streamthoughts.jikkou.kafka.control.handlers.acls.AclChangeDescription;
import io.streamthoughts.jikkou.kafka.control.handlers.acls.CreateAclChangeHandler;
import io.streamthoughts.jikkou.kafka.control.handlers.acls.DeleteAclChangeHandler;
import io.streamthoughts.jikkou.kafka.control.handlers.acls.KafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.control.handlers.acls.builder.LiteralKafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.control.handlers.acls.builder.TopicMatchingAclRulesBuilder;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAclChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAclChangeList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.Collection;
import java.util.List;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1KafkaPrincipalAuthorization.class)
@SupportedReconciliationModes(modes = {CREATE, DELETE, APPLY})
public final class AdminClientKafkaAclController extends AbstractAdminClientKafkaController
        implements BaseExternalResourceController<V1KafkaPrincipalAuthorization, AclChange> {

    private AdminClientKafkaAclCollector descriptor;

    /**
     * Creates a new {@link AdminClientKafkaAclController} instance.
     */
    public AdminClientKafkaAclController() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaAclController} instance.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaAclController(final @NotNull Configuration config) {
        super(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaAclController} instance.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaAclController(final @NotNull AdminClientContext adminClientContext) {
        super(adminClientContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        super.configure(config);
        this.descriptor = new AdminClientKafkaAclCollector(adminClientContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V1KafkaAclChangeList computeReconciliationChanges(@NotNull Collection<V1KafkaPrincipalAuthorization> resources,
                                                             @NotNull ReconciliationMode mode,
                                                             @NotNull ReconciliationContext context) {


        AdminClient adminClient = adminClientContext.client();

        // Get the list of remote resources that are candidates for this reconciliation
        List<V1KafkaPrincipalAuthorization> expectedStates = resources
                .stream()
                .filter(new AggregateSelector(context.selectors())::apply)
                .toList();

        // Get the actual state from the cluster.
        List<V1KafkaPrincipalAuthorization> actualStates = descriptor.listAll(adminClient)
                .stream()
                .filter(new AggregateSelector(context.selectors())::apply)
                .toList();

        // Compute state changes
        final KafkaAclBindingBuilder builder = KafkaAclBindingBuilder.combines(
                new LiteralKafkaAclBindingBuilder(),
                new TopicMatchingAclRulesBuilder(adminClient)
        );

        List<AclChange> changes = new AclChangeComputer(builder)
                .computeChanges(actualStates, expectedStates);

        return new V1KafkaAclChangeList()
                .withItems(changes.stream().map(this::toModelChange).toList());
    }

    private V1KafkaAclChange toModelChange(AclChange c) {
        return V1KafkaAclChange.builder().withChange(c).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResult<AclChange>> execute(@NotNull List<AclChange> changes,
                                                 @NotNull ReconciliationMode mode,
                                                 boolean dryRun) {
        AdminClient client = adminClientContext.client();
        List<ChangeHandler<AclChange>> handlers = List.of(
                new CreateAclChangeHandler(client),
                new DeleteAclChangeHandler(client),
                new ChangeHandler.None<>(AclChangeDescription::new)
        );
        return new ChangeExecutor<>(handlers).execute(changes, dryRun);
    }
}
