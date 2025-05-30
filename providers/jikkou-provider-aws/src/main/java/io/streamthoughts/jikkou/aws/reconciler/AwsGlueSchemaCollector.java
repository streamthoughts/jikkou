/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws.reconciler;

import io.streamthoughts.jikkou.aws.AwsExtensionProvider;
import io.streamthoughts.jikkou.aws.AwsGlueLabelsAndAnnotations;
import io.streamthoughts.jikkou.aws.model.Compatibility;
import io.streamthoughts.jikkou.aws.models.AwsGlueSchema;
import io.streamthoughts.jikkou.aws.models.AwsGlueSchemaSpec;
import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.data.SchemaHandle;
import io.streamthoughts.jikkou.core.data.SchemaType;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.GetSchemaRequest;
import software.amazon.awssdk.services.glue.model.GetSchemaResponse;
import software.amazon.awssdk.services.glue.model.GetSchemaVersionRequest;
import software.amazon.awssdk.services.glue.model.GetSchemaVersionResponse;
import software.amazon.awssdk.services.glue.model.GlueException;
import software.amazon.awssdk.services.glue.model.ListRegistriesRequest;
import software.amazon.awssdk.services.glue.model.ListSchemasRequest;
import software.amazon.awssdk.services.glue.model.ListSchemasResponse;
import software.amazon.awssdk.services.glue.model.RegistryId;
import software.amazon.awssdk.services.glue.model.RegistryListItem;
import software.amazon.awssdk.services.glue.model.SchemaId;
import software.amazon.awssdk.services.glue.model.SchemaListItem;
import software.amazon.awssdk.services.glue.model.SchemaVersionNumber;

@SupportedResource(type = AwsGlueSchema.class)
public class AwsGlueSchemaCollector extends ContextualExtension implements Collector<AwsGlueSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(AwsGlueSchemaCollector.class);

    public interface Config {
        ConfigProperty<String> REGISTRY_NAME = ConfigProperty
            .ofString("registryName")
            .description("Specifies the registry name.")
            .required(false);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Optional<AwsGlueSchema> get(@NotNull String name, @NotNull Configuration configuration) {

        final String registryName = Config.REGISTRY_NAME.getOptional(configuration)
            .orElseThrow(() -> new ConfigException(
                "The '%s' configuration property is required for %s".formatted(
                    Config.REGISTRY_NAME.key(),
                    AwsGlueSchemaCollector.class.getSimpleName()
                )
            ));

        final AwsExtensionProvider provider = extensionContext().provider();
        try (GlueClient glueClient = provider.newGlueClient()) {
            return Optional.ofNullable(fetchSchema(name, registryName, glueClient));
        }
    }

    private AwsGlueSchema fetchSchema(@NotNull String schemaName,
                                      @NotNull String registryName,
                                      @NotNull GlueClient glueClient) {

        GetSchemaRequest getSchemaReq = GetSchemaRequest.builder()
            .schemaId(SchemaId.builder()
                .registryName(registryName)
                .schemaName(schemaName)
                .build())
            .build();

        GetSchemaResponse schema = glueClient.getSchema(getSchemaReq);

        long latestVersion = schema.latestSchemaVersion();

        GetSchemaVersionResponse versionResponse = glueClient.getSchemaVersion(GetSchemaVersionRequest.builder()
            .schemaId(SchemaId.builder()
                .registryName(registryName)
                .schemaName(schemaName)
                .build())
            .schemaVersionNumber(SchemaVersionNumber.builder()
                .versionNumber(latestVersion)
                .build())
            .build());

        return AwsGlueSchema.builder()
            .withMetadata(ObjectMeta
                .builder()
                .withName(schemaName)
                .withLabel(AwsGlueLabelsAndAnnotations.SCHEMA_REGISTRY_NAME, schema.registryName())
                .withAnnotation(AwsGlueLabelsAndAnnotations.SCHEMA_CREATED_TIME, schema.createdTime())
                .withAnnotation(AwsGlueLabelsAndAnnotations.SCHEMA_UPDATED_TIME, schema.updatedTime())
                .withAnnotation(AwsGlueLabelsAndAnnotations.SCHEMA_REGISTRY_ARN, schema.registryArn())
                .withAnnotation(AwsGlueLabelsAndAnnotations.SCHEMA_SCHEMA_ARN, schema.schemaArn())
                .withAnnotation(AwsGlueLabelsAndAnnotations.SCHEMA_SCHEMA_VERSION_ID, versionResponse.schemaVersionId())
                .build()
            )
            .withSpec(AwsGlueSchemaSpec
                .builder()
                .withCompatibility(Compatibility.valueOf(schema.compatibilityAsString()))
                .withDataFormat(SchemaType.valueOf(schema.dataFormatAsString()))
                .withSchemaDefinition(new SchemaHandle(versionResponse.schemaDefinition()))
                .withDescription(schema.description())
                .build()
            )
            .build();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceList<AwsGlueSchema> listAll(@NotNull Configuration configuration, @NotNull Selector selector) {

        Set<String> registryNames = AwsExtensionProvider.Config.GLUE_REGISTRIES
            .getOptional(extensionContext().configuration())
            .map(HashSet::new).orElse(new HashSet<>());

        return listAll(registryNames, selector);
    }

    public ResourceList<AwsGlueSchema> listAll(Set<String> registryNames, @NotNull Selector selector) {
        final AwsExtensionProvider provider = extensionContext().provider();
        try (GlueClient glueClient = provider.newGlueClient()) {
            if (registryNames.isEmpty()) {
                // List registries
                ListRegistriesRequest request = ListRegistriesRequest.builder().build();
                registryNames = glueClient.listRegistries(request).registries()
                    .stream()
                    .map(RegistryListItem::registryName)
                    .collect(Collectors.toSet());
            }

            List<Pair<String, String>> registryNameAndSchemaNames = registryNames.stream()
                .flatMap(registryName -> {
                    LOG.debug("Listing schema definitions from registry {}", registryName);
                    // List schemas
                    ListSchemasResponse list = glueClient.listSchemas(
                        ListSchemasRequest.builder()
                            .registryId(RegistryId.builder().registryName(registryName).build())
                            .build()
                    );

                    List<SchemaListItem> schemas = list.schemas();
                    LOG.debug("Found {} schema definitions from registry {} ", schemas.size(), registryName);
                    return schemas.stream().map(it -> Pair.of(it.schemaName(), it.registryName()));
                }).toList();
            return listAll(glueClient, registryNameAndSchemaNames, selector);
        } catch (GlueException e) {
            throw new JikkouRuntimeException("Unable to list AWS Glue Schemas", e);
        }
    }

    private ResourceList<AwsGlueSchema> listAll(GlueClient glueClient, List<Pair<String, String>> registryNameAndSchemaNames, @NotNull Selector selector) {
        List<AwsGlueSchema> resources = new LinkedList<>();
        for (Pair<String, String> item : registryNameAndSchemaNames) {
            resources.add(fetchSchema(item._1(), item._2(), glueClient));
        }
        return ResourceList.of(resources);
    }
}
