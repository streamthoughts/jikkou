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
package io.streamthoughts.jikkou.extension.aiven.control;

import static io.streamthoughts.jikkou.core.ReconciliationMode.APPLY_ALL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.AcceptsResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.models.GenericResourceListObject;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconcilier.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconcilier.ChangeHandler;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import io.streamthoughts.jikkou.core.reconcilier.Controller;
import io.streamthoughts.jikkou.core.reconcilier.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.core.selectors.AggregateSelector;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.change.KafkaAclEntryChangeComputer;
import io.streamthoughts.jikkou.extension.aiven.change.KafkaChangeDescriptions;
import io.streamthoughts.jikkou.extension.aiven.change.handler.CreateKafkaAclEntryChangeHandler;
import io.streamthoughts.jikkou.extension.aiven.change.handler.DeleteKafkaAclEntryChangeHandler;
import io.streamthoughts.jikkou.extension.aiven.converter.V1KafkaAclEntryListConverter;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntryList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, APPLY_ALL}
)
@AcceptsResource(type = V1KafkaTopicAclEntry.class)
@AcceptsResource(type = V1KafkaTopicAclEntryList.class, converter = V1KafkaAclEntryListConverter.class)
public class AivenKafkaTopicAclEntryController implements Controller<V1KafkaTopicAclEntry, ValueChange<KafkaAclEntry>> {

    public static final ConfigProperty<Boolean> DELETE_ORPHANS_OPTIONS = ConfigProperty
            .ofBoolean("delete-orphans")
            .orElse(false);

    private AivenApiClientConfig config;
    private AivenKafkaTopicAclEntryCollector collector;

    /**
     * Creates a new {@link AivenKafkaTopicAclEntryController} instance.
     */
    public AivenKafkaTopicAclEntryController() {
    }

    /**
     * Creates a new {@link AivenKafkaTopicAclEntryController} instance.
     *
     * @param config the schema registry client configuration.
     */
    public AivenKafkaTopicAclEntryController(@NotNull AivenApiClientConfig config) {
        configure(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        configure(new AivenApiClientConfig(config));
    }

    private void configure(@NotNull AivenApiClientConfig config) throws ConfigException {
        this.config = config;
        this.collector = new AivenKafkaTopicAclEntryCollector(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult<ValueChange<KafkaAclEntry>>> execute(@NotNull final ChangeExecutor<ValueChange<KafkaAclEntry>> executor, @NotNull ReconciliationContext context) {

        AivenApiClient api = AivenApiClientFactory.create(config);
        try {
            List<ChangeHandler<ValueChange<KafkaAclEntry>>> handlers = List.of(
                    new CreateKafkaAclEntryChangeHandler(api),
                    new DeleteKafkaAclEntryChangeHandler(api),
                    new ChangeHandler.None<>(it -> KafkaChangeDescriptions.of(
                            it.getChange().operation(),
                            it.getChange().getAfter())
                    )
            );
            return executor.execute(handlers);
        } finally {
            api.close();
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceListObject<? extends HasMetadataChange<ValueChange<KafkaAclEntry>>> plan(
            @NotNull Collection<V1KafkaTopicAclEntry> resources,
            @NotNull ReconciliationContext context) {

        // Get existing resources from the environment.
        List<V1KafkaTopicAclEntry> actualResources = collector.listAll(context.configuration()).stream()
                .filter(new AggregateSelector(context.selectors())::apply)
                .toList();

        // Get expected resources which are candidates for this reconciliation.
        List<V1KafkaTopicAclEntry> expectedResources = resources.stream()
                .filter(new AggregateSelector(context.selectors())::apply)
                .toList();

        Boolean deleteOrphans = DELETE_ORPHANS_OPTIONS.evaluate(context.configuration());
        KafkaAclEntryChangeComputer computer = new KafkaAclEntryChangeComputer(deleteOrphans);

        List<HasMetadataChange<ValueChange<KafkaAclEntry>>> changes = computer
                .computeChanges(actualResources, expectedResources);

        return GenericResourceListObject
                .<HasMetadataChange<ValueChange<KafkaAclEntry>>>builder()
                .withItems(changes)
                .build();
    }
}
