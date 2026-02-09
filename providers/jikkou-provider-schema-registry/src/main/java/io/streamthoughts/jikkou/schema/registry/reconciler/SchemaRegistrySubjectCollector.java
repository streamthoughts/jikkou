/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.reconciler;

import io.streamthoughts.jikkou.common.utils.Enums;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryExtensionProvider;
import io.streamthoughts.jikkou.schema.registry.V1SchemaRegistrySubjectFactory;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.DefaultAsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApiFactory;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityLevelObject;
import io.streamthoughts.jikkou.schema.registry.api.data.ModeObject;
import io.streamthoughts.jikkou.schema.registry.collections.V1SchemaRegistrySubjectList;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.Modes;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SupportedResource(type = V1SchemaRegistrySubject.class)
public class SchemaRegistrySubjectCollector extends ContextualExtension implements Collector<V1SchemaRegistrySubject> {

    public static final String EMPTY_STRING = "";

    public interface Config {
        ConfigProperty<Boolean> DEFAULT_GLOBAL_COMPATIBILITY_LEVEL = ConfigProperty
            .ofBoolean("default-to-global-compatibility-level")
            .description("Specifies whether to default to global compatibility.")
            .required(false)
            .defaultValue(true);
    }

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

        try (AsyncSchemaRegistryApi api = new DefaultAsyncSchemaRegistryApi(SchemaRegistryApiFactory.create(config))) {
            return listAll(configuration, api.listSubjects().flatMapMany(Flux::fromIterable), api);
        }
    }

    public ResourceList<V1SchemaRegistrySubject> listAll(@NotNull Configuration configuration, @NotNull List<String> subjects) {
        try (AsyncSchemaRegistryApi api = new DefaultAsyncSchemaRegistryApi(SchemaRegistryApiFactory.create(config))) {
            return listAll(configuration, Flux.fromIterable(subjects), api);
        }
    }

    private ResourceList<V1SchemaRegistrySubject> listAll(@NotNull Configuration configuration,
                                                          @NotNull Flux<String> subjects,
                                                          @NotNull AsyncSchemaRegistryApi api) {
        Flux<V1SchemaRegistrySubject> flux =
            subjects
                // Get Schema Registry Latest Subject Version
                .flatMap(subject -> api.getLatestSubjectSchema(subject).onErrorResume(SchemaRegistrySubjectCollector::emptyOn404))
                .flatMap(subjectSchemaVersion -> {
                    // Get Schema Registry Subject Compatibility
                    Mono<String> compatibilityMono =
                        api.getSubjectCompatibilityLevel(subjectSchemaVersion.subject(), Config.DEFAULT_GLOBAL_COMPATIBILITY_LEVEL.get(configuration))
                            .map(CompatibilityLevelObject::compatibilityLevel)
                            .onErrorResume(SchemaRegistrySubjectCollector::emptyOn404);
                    // Get Schema Registry Subject Mode
                    Mono<String> modeMono =
                        api.getSubjectMode(subjectSchemaVersion.subject())
                            .map(ModeObject::mode)
                            .onErrorResume(SchemaRegistrySubjectCollector::emptyOn404);

                    return Mono.zip(compatibilityMono.defaultIfEmpty(EMPTY_STRING), modeMono.defaultIfEmpty(EMPTY_STRING))
                        .map(tuple -> schemaRegistrySubjectFactory.createSchemaRegistrySubject(
                            subjectSchemaVersion,
                            Enums.safeValueOf(CompatibilityLevels.class, tuple.getT1()),
                            Enums.safeValueOf(Modes.class, tuple.getT2())
                        ));
                });
        try {
            return new V1SchemaRegistrySubjectList.Builder().withItems(flux.collectList().block()).build();
        } catch (Exception e) {
            throw new JikkouRuntimeException("Failed to list all schema registry subject versions", e);
        }
    }

    private static boolean isNotFound(final WebApplicationException exception) {
        return exception.getResponse().getStatus() == 404;
    }

    SchemaRegistrySubjectCollector prettyPrintSchema(final boolean prettyPrintSchema) {
        this.prettyPrintSchema = prettyPrintSchema;
        return this;
    }

    private static <T> Mono<T> emptyOn404(Throwable t) {
        return t instanceof WebApplicationException wae && isNotFound(wae)
            ? Mono.empty()
            : Mono.error(t);
    }
}
