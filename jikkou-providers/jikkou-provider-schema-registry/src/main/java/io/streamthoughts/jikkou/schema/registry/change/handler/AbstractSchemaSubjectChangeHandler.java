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

import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_COMPATIBILITY_LEVEL;
import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_REFERENCES;
import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_SCHEMA;
import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_SCHEMA_TYPE;

import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.StateChangeList;
import io.streamthoughts.jikkou.core.reconciler.ChangeError;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.http.client.RestClientException;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryAnnotations;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityObject;
import io.streamthoughts.jikkou.schema.registry.api.data.ErrorResponse;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaReference;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeDescription;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeOptions;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSchemaSubjectChangeHandler implements ChangeHandler<ResourceChange> {

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

    protected CompletableFuture<Void> updateCompatibilityLevel(ResourceChange change) {
        final CompatibilityLevels compatibilityLevels = StateChangeList
                .of(change.getSpec().getChanges())
                .getLast(DATA_COMPATIBILITY_LEVEL, TypeConverter.of(CompatibilityLevels.class))
                .getAfter();

        final String subjectName = change.getMetadata().getName();
        LOG.info("Updating compatibility-level for Schema Registry subject '{}'.",
                subjectName
        );
        return api
                .updateSubjectCompatibilityLevel(subjectName, new CompatibilityObject(compatibilityLevels.name()))
                .thenApply(compatibilityObject -> {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                                "Updated compatibility-level for Schema Registry subject '{}' to '{}'.",
                                subjectName,
                                compatibilityObject.compatibility());
                    }
                    return null;
                });
    }

    protected CompletableFuture<Void> registerSubjectVersion(@NotNull ResourceChange change) {
        String schema = change.getSpec()
                .getChanges()
                .getLast(DATA_SCHEMA, TypeConverter.String())
                .getAfter();

        SchemaType type = change.getSpec()
                .getChanges()
                .getLast(DATA_SCHEMA_TYPE, TypeConverter.of(SchemaType.class))
                .getAfter();


        SchemaSubjectChangeOptions options = getSchemaSubjectChangeOptions(change);

        final String subjectName = change.getMetadata().getName();
        if (LOG.isInfoEnabled()) {
            LOG.info("Registering new Schema Registry subject version: subject '{}', optimization={}, schema={}.",
                    subjectName,
                    options.normalizeSchema(),
                    schema
            );
        }

        List<SubjectSchemaReference> references = change.getSpec()
                .getChanges()
                .getLast(DATA_REFERENCES, TypeConverter.ofList(SubjectSchemaReference.class))
                .getAfter();

        return api
                .registerSubjectVersion(
                        subjectName,
                        new SubjectSchemaRegistration(schema, type, references),
                        options.normalizeSchema()
                )
                .thenApply(subjectSchemaId -> {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                                "Registered Schema Registry subject version: subject '{}', id '{}'.",
                                subjectName,
                                subjectSchemaId.id()
                        );
                        change.getMetadata()
                                .addAnnotationIfAbsent(
                                        SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_ID,
                                        subjectSchemaId.id()
                                );
                    }
                    return null;
                });
    }

    protected SchemaSubjectChangeOptions getSchemaSubjectChangeOptions(@NotNull ResourceChange change) {
        return TypeConverter
                .of(SchemaSubjectChangeOptions.class)
                .convertValue(change.getSpec().getData());
    }

    protected CompletableFuture<Void> deleteCompatibilityLevel(@NotNull ResourceChange change) {
        final String subject = change.getMetadata().getName();
        if (LOG.isInfoEnabled()) {
            LOG.info("Deleting compatibility-level for Schema Registry subject '{}'.",
                    subject
            );
        }
        return api
                .deleteSubjectCompatibilityLevel(subject)
                .thenApplyAsync(compatibilityObject -> {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                                "Deleted compatibility-level for Schema Registry subject '{}' to '{}'.",
                                change.getMetadata().getName(),
                                compatibilityObject.compatibility());
                    }
                    return null;
                });
    }

    public ChangeResponse<ResourceChange> toChangeResponse(ResourceChange change,
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
    public TextDescription describe(@NotNull ResourceChange item) {
        return new SchemaSubjectChangeDescription(item);
    }
}
