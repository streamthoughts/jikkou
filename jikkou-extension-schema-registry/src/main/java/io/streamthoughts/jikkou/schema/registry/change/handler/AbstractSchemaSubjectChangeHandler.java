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
package io.streamthoughts.jikkou.schema.registry.change.handler;

import io.streamthoughts.jikkou.api.change.ChangeDescription;
import io.streamthoughts.jikkou.api.change.ChangeError;
import io.streamthoughts.jikkou.api.change.ChangeHandler;
import io.streamthoughts.jikkou.api.change.ChangeMetadata;
import io.streamthoughts.jikkou.api.change.ChangeResponse;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.rest.client.RestClientException;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityObject;
import io.streamthoughts.jikkou.schema.registry.api.data.ErrorResponse;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChange;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeDescription;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeOptions;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSchemaSubjectChangeHandler implements ChangeHandler<SchemaSubjectChange> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSchemaSubjectChangeHandler.class);

    protected final AsyncSchemaRegistryApi api;

    /**
     * Creates a new {@link AbstractSchemaSubjectChangeHandler} instance.
     *
     * @param api the {@link SchemaRegistryApi} instance.
     */
    public AbstractSchemaSubjectChangeHandler(@NotNull final SchemaRegistryApi api) {
        this.api = new AsyncSchemaRegistryApi(Objects.requireNonNull(api, "api must not be null"));
    }

    protected CompletableFuture<Void> updateCompatibilityLevel(SchemaSubjectChange change) {
        CompatibilityLevels compatibilityLevels = change.getCompatibilityLevels().getAfter();
        return api
                .updateConfigCompatibility(change.getSubject(), new CompatibilityObject(compatibilityLevels.name()))
                .thenApplyAsync(compatibilityObject -> {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                                "Updated compatibility for subject '{}' to '{}'",
                                change.getSubject(),
                                compatibilityObject.compatibility());
                    }
                    return null;
                });
    }

    protected CompletableFuture<Void> registerSubjectSchema(@NotNull SchemaSubjectChange change) {
        String schema = change.getSchema().getAfter();
        SchemaType type = change.getSchemaType().getAfter();
        SchemaSubjectChangeOptions options = change.getOptions();
        LOG.info("Registering new schema: subject '{}', optimization={}, schema={}",
                change.getSubject(),
                options.isSchemaOptimizationEnabled(),
                schema
        );
        return api.registerSubjectVersion(
                        change.getSubject(),
                        new SubjectSchemaRegistration(schema, type, change.getReferences().getAfter()),
                        options.isSchemaOptimizationEnabled()
                )
                .thenApplyAsync(subjectSchemaId -> {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                                "Registered new schema: subject '{}', id '{}'",
                                change.getSubject(),
                                subjectSchemaId.id()
                        );
                    }
                    return null;
                });
    }

    public ChangeResponse<SchemaSubjectChange> toChangeResponse(HasMetadataChange<SchemaSubjectChange> change,
                                                                CompletableFuture<?> future) {
        CompletableFuture<ChangeMetadata> handled = future.handle((unused, throwable) -> {
            if (throwable == null) {
                return ChangeMetadata.empty();
            }
            if (throwable instanceof RestClientException e) {
                ErrorResponse error = e.getResponseEntity(ErrorResponse.class);
                return new ChangeMetadata(new ChangeError(error.message(), error.errorCode()));
            }
            return ChangeMetadata.of(throwable);
        });

        return new ChangeResponse<>(change, handled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeDescription getDescriptionFor(@NotNull HasMetadataChange<SchemaSubjectChange> item) {
        return new SchemaSubjectChangeDescription(item);
    }
}
