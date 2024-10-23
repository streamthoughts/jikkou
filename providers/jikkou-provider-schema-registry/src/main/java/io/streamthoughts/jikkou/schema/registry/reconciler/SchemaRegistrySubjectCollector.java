/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.reconciler;

import io.streamthoughts.jikkou.common.utils.AsyncUtils;
import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionOptionSpec;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionSpec;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.http.client.RestClientException;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryExtensionProvider;
import io.streamthoughts.jikkou.schema.registry.V1SchemaRegistrySubjectFactory;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.DefaultAsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApiFactory;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import io.streamthoughts.jikkou.schema.registry.collections.V1SchemaRegistrySubjectList;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@ExtensionSpec(
    options = {
        @ExtensionOptionSpec(
            name = SchemaRegistrySubjectCollector.DEFAULT_TO_GLOBAL_COMPATIBILITY_LEVEL,
            description = SchemaRegistrySubjectCollector.DEFAULT_TO_GLOBAL_COMPATIBILITY_LEVEL,
            type = Boolean.class,
            defaultValue = "true"
        )
    }
)
@SupportedResource(type = V1SchemaRegistrySubject.class)
public class SchemaRegistrySubjectCollector extends ContextualExtension implements Collector<V1SchemaRegistrySubject> {

    public static final String DEFAULT_TO_GLOBAL_COMPATIBILITY_LEVEL = "default-to-global-compatibility-level";

    private SchemaRegistryClientConfig config;

    private boolean prettyPrintSchema = true;

    private V1SchemaRegistrySubjectFactory schemaRegistrySubjectFactory;

    /**
     * Creates a new {@link SchemaRegistrySubjectCollector} instance.
     */
    public SchemaRegistrySubjectCollector() {
    }

    /**
     * Creates a new {@link SchemaRegistrySubjectCollector} instance.
     *
     * @param config the configuration.
     */
    public SchemaRegistrySubjectCollector(SchemaRegistryClientConfig config) {
        init(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        init(context.<SchemaRegistryExtensionProvider>provider().clientConfig());
    }

    private void init(@NotNull SchemaRegistryClientConfig config) {
        this.config = config;
        this.schemaRegistrySubjectFactory = new V1SchemaRegistrySubjectFactory(
            config.vendor(),
            config.url(),
            prettyPrintSchema
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceList<V1SchemaRegistrySubject> listAll(@NotNull Configuration configuration,
                                                         @NotNull Selector selector) {

        Boolean defaultToGlobalCompatibilityLevel = extensionContext()
            .<Boolean>configProperty(DEFAULT_TO_GLOBAL_COMPATIBILITY_LEVEL)
            .get(configuration);

        AsyncSchemaRegistryApi api = new DefaultAsyncSchemaRegistryApi(SchemaRegistryApiFactory.create(config));
        try {
            CompletableFuture<List<V1SchemaRegistrySubject>> result = api
                .listSubjects()
                .thenComposeAsync(subjects -> AsyncUtils.waitForAll(getAllSchemaRegistrySubjectsAsync(subjects, api, defaultToGlobalCompatibilityLevel)));
            Optional<Throwable> exception = AsyncUtils.getException(result);
            if (exception.isPresent()) {
                throw new JikkouRuntimeException(
                    "Failed to list all schema registry subject versions",
                    exception.get()
                );
            }
            List<V1SchemaRegistrySubject> resources = result.join()
                .stream()
                .filter(selector::apply)
                .collect(Collectors.toList());
            return new V1SchemaRegistrySubjectList.Builder().withItems(resources).build();
        } finally {
            api.close();
        }
    }

    SchemaRegistrySubjectCollector prettyPrintSchema(final boolean prettyPrintSchema) {
        this.prettyPrintSchema = prettyPrintSchema;
        return this;
    }

    @NotNull
    private List<CompletableFuture<V1SchemaRegistrySubject>> getAllSchemaRegistrySubjectsAsync(final List<String> subjects,
                                                                                               final AsyncSchemaRegistryApi api,
                                                                                               boolean defaultToGlobalCompatibilityLevel) {
        return subjects.stream()
            .map(subject -> getSchemaRegistrySubjectAsync(
                api, subject, defaultToGlobalCompatibilityLevel, schemaRegistrySubjectFactory))
            .toList();
    }

    @NotNull
    private static CompletableFuture<V1SchemaRegistrySubject> getSchemaRegistrySubjectAsync(@NotNull AsyncSchemaRegistryApi api,
                                                                                            @NotNull String subject,
                                                                                            boolean defaultToGlobalCompatibilityLevel,
                                                                                            @NotNull V1SchemaRegistrySubjectFactory factory) {

        return api
            // Get Schema Registry Latest Subject Version
            .getLatestSubjectSchema(subject)
            // Get Schema Registry Subject Compatibility
            .thenCompose(subjectSchemaVersion -> api
                .getSubjectCompatibilityLevel(subject, defaultToGlobalCompatibilityLevel)
                .thenApply(compatibilityObject -> CompatibilityLevels.valueOf(compatibilityObject.compatibilityLevel()))
                .exceptionally(t -> {
                        if (t.getCause() != null) t = t.getCause();

                        if (t instanceof RestClientException exception) {
                            if (exception.response()
                                .map(Response::getStatus)
                                .filter(status -> status.equals(404))
                                .isPresent()) {
                                return null;
                            }
                        }
                        if (t instanceof RuntimeException re) {
                            throw re;
                        }
                        throw new JikkouRuntimeException(t);
                    }
                )
                .thenApply(compatibilityLevelObject ->
                    Pair.of(subjectSchemaVersion, compatibilityLevelObject)
                )
            )
            // Create SchemaRegistrySubject object
            .thenApply(tuple ->
                factory.createSchemaRegistrySubject(
                    tuple._1(),
                    tuple._2()
                )
            );
    }
}
