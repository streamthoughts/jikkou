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
package io.streamthoughts.jikkou.extension.aiven.reconcilier;

import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.FULL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.UPDATE;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconcilier.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconcilier.ChangeHandler;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import io.streamthoughts.jikkou.core.reconcilier.Controller;
import io.streamthoughts.jikkou.core.reconcilier.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaQuotaEntry;
import io.streamthoughts.jikkou.extension.aiven.change.KafkaChangeDescriptions;
import io.streamthoughts.jikkou.extension.aiven.change.KafkaQuotaChangeComputer;
import io.streamthoughts.jikkou.extension.aiven.change.handler.CreateKafkaQuotaChangeHandler;
import io.streamthoughts.jikkou.extension.aiven.change.handler.DeleteKafkaQuotaChangeHandler;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
@SupportedResource(type = V1KafkaQuota.class)
public class AivenKafkaQuotaController implements Controller<V1KafkaQuota, ValueChange<KafkaQuotaEntry>> {

    public static final ConfigProperty<Boolean> DELETE_ORPHANS_OPTIONS = ConfigProperty
            .ofBoolean("delete-orphans")
            .orElse(false);

    private AivenApiClientConfig config;
    private AivenKafkaQuotaCollector collector;

    /**
     * Creates a new {@link AivenKafkaQuotaController} instance.
     */
    public AivenKafkaQuotaController() {
    }

    /**
     * Creates a new {@link AivenKafkaQuotaController} instance.
     *
     * @param config the schema registry client configuration.
     */
    public AivenKafkaQuotaController(@NotNull AivenApiClientConfig config) {
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
        this.collector = new AivenKafkaQuotaCollector(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult<ValueChange<KafkaQuotaEntry>>> execute(@NotNull final ChangeExecutor<ValueChange<KafkaQuotaEntry>> executor, @NotNull ReconciliationContext context) {

        AivenApiClient api = AivenApiClientFactory.create(config);
        try {
            List<ChangeHandler<ValueChange<KafkaQuotaEntry>>> handlers = List.of(
                    new CreateKafkaQuotaChangeHandler(api),
                    new DeleteKafkaQuotaChangeHandler(api),
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
    public ResourceListObject<? extends HasMetadataChange<ValueChange<KafkaQuotaEntry>>> plan(
            @NotNull Collection<V1KafkaQuota> resources,
            @NotNull ReconciliationContext context) {

        // Get existing resources from the environment.
        List<V1KafkaQuota> actualResources = collector.listAll(context.configuration(), Selectors.NO_SELECTOR).stream()
                .filter(context.selector()::apply)
                .toList();

        // Get expected resources which are candidates for this reconciliation.
        List<V1KafkaQuota> expectedResources = resources.stream()
                .filter(context.selector()::apply)
                .toList();

        Boolean deleteOrphans = DELETE_ORPHANS_OPTIONS.evaluate(context.configuration());
        KafkaQuotaChangeComputer computer = new KafkaQuotaChangeComputer(deleteOrphans);

        List<HasMetadataChange<ValueChange<KafkaQuotaEntry>>> changes = computer
                .computeChanges(actualResources, expectedResources);

        return DefaultResourceListObject
                .<HasMetadataChange<ValueChange<KafkaQuotaEntry>>>builder()
                .withItems(changes)
                .build();
    }
}
