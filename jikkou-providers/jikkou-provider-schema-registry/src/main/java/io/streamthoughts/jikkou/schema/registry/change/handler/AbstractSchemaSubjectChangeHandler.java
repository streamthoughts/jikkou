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

import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.reconcilier.ChangeDescription;
import io.streamthoughts.jikkou.core.reconcilier.ChangeError;
import io.streamthoughts.jikkou.core.reconcilier.ChangeHandler;
import io.streamthoughts.jikkou.core.reconcilier.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResponse;
import io.streamthoughts.jikkou.http.client.RestClientException;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryAnnotations;
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
    public AbstractSchemaSubjectChangeHandler(@NotNull final AsyncSchemaRegistryApi api) {
        this.api = Objects.requireNonNull(api, "api must not be null");
    }

    protected CompletableFuture<Void> updateCompatibilityLevel(SchemaSubjectChange change) {
        CompatibilityLevels compatibilityLevels = change.getCompatibilityLevels().getAfter();
        LOG.info("Updating compatibility-level for Schema Registry subject '{}'.",
                change.getSubject()
        );
        return api
                .updateSubjectCompatibilityLevel(change.getSubject(), new CompatibilityObject(compatibilityLevels.name()))
                .thenApply(compatibilityObject -> {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                                "Updated compatibility-level for Schema Registry subject '{}' to '{}'.",
                                change.getSubject(),
                                compatibilityObject.compatibility());
                    }
                    return null;
                });
    }

    protected CompletableFuture<Void> registerSubjectVersion(@NotNull HasMetadataChange<SchemaSubjectChange> item) {
        SchemaSubjectChange change = item.getChange();
        String schema = change.getSchema().getAfter();
        SchemaType type = change.getSchemaType().getAfter();
        SchemaSubjectChangeOptions options = change.getOptions();

        final String subject = change.getSubject();
        LOG.info("Registering new Schema Registry subject version: subject '{}', optimization={}, schema={}.",
                subject,
                options.isSchemaOptimizationEnabled(),
                schema
        );
        return api
                .registerSubjectVersion(
                        subject,
                        new SubjectSchemaRegistration(schema, type, change.getReferences().getAfter()),
                        options.isSchemaOptimizationEnabled()
                )
                .thenApply(subjectSchemaId -> {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                                "Registered Schema Registry subject version: subject '{}', id '{}'.",
                                subject,
                                subjectSchemaId.id()
                        );
                        item.getMetadata()
                                .addAnnotationIfAbsent(
                                        SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_ID,
                                        subjectSchemaId.id()
                                );
                    }
                    return null;
                });
    }

    protected CompletableFuture<Void> deleteCompatibilityLevel(@NotNull SchemaSubjectChange change) {
        LOG.info("Deleting compatibility-level for Schema Registry subject '{}'.",
                change.getSubject()
        );
        return api
                .deleteSubjectCompatibilityLevel(change.getSubject())
                .thenApplyAsync(compatibilityObject -> {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                                "Deleted compatibility-level for Schema Registry subject '{}' to '{}'.",
                                change.getSubject(),
                                compatibilityObject.compatibility());
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

            if (throwable.getCause() != null) {
                throwable = throwable.getCause();
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
