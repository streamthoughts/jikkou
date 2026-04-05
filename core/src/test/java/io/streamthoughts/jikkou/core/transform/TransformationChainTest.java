/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.generics.GenericResource;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TransformationChainTest {

    private static final ReconciliationContext EMPTY_CONTEXT = ReconciliationContext.Default.EMPTY;

    @Test
    void shouldPreserveProviderAnnotationWhenTransformationMutatesIt() {
        // A transformation that tries to change the provider annotation
        Transformation<HasMetadata> mutatingTransformation = new Transformation<>() {
            @Override
            public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata resource,
                                                            @NotNull HasItems resources,
                                                            @NotNull ReconciliationContext context) {
                return Optional.of(HasMetadata.addMetadataAnnotation(resource,
                        CoreAnnotations.JIKKOU_IO_PROVIDER, "kafka-hacked"));
            }
        };

        TransformationChain chain = new TransformationChain(List.of(mutatingTransformation));

        GenericResource resource = new GenericResource(
                "core/v1",
                "Test",
                ObjectMeta.builder()
                        .withName("test")
                        .withAnnotation(CoreAnnotations.JIKKOU_IO_PROVIDER, "kafka-prod")
                        .build(),
                null
        );

        HasItems otherResources = ResourceList.empty();
        List<HasMetadata> result = chain.transformAll(List.of(resource), otherResources, EMPTY_CONTEXT);

        Assertions.assertEquals(1, result.size());
        Optional<String> provider = CoreAnnotations.findProviderAnnotation(result.getFirst());
        Assertions.assertTrue(provider.isPresent());
        Assertions.assertEquals("kafka-prod", provider.get(), "Provider annotation should be restored to original value");
    }

    @Test
    void shouldPreserveProviderAnnotationWhenTransformationRemovesIt() {
        // A transformation that removes all annotations
        Transformation<HasMetadata> removingTransformation = new Transformation<>() {
            @Override
            public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata resource,
                                                            @NotNull HasItems resources,
                                                            @NotNull ReconciliationContext context) {
                return Optional.of(resource.withMetadata(
                        ObjectMeta.builder().withName(resource.getMetadata().getName()).build()));
            }
        };

        TransformationChain chain = new TransformationChain(List.of(removingTransformation));

        GenericResource resource = new GenericResource(
                "core/v1",
                "Test",
                ObjectMeta.builder()
                        .withName("test")
                        .withAnnotation(CoreAnnotations.JIKKOU_IO_PROVIDER, "kafka-prod")
                        .build(),
                null
        );

        HasItems otherResources = ResourceList.empty();
        List<HasMetadata> result = chain.transformAll(List.of(resource), otherResources, EMPTY_CONTEXT);

        Assertions.assertEquals(1, result.size());
        Optional<String> provider = CoreAnnotations.findProviderAnnotation(result.getFirst());
        Assertions.assertTrue(provider.isPresent());
        Assertions.assertEquals("kafka-prod", provider.get(), "Provider annotation should be restored even when transformation removes it");
    }

    @Test
    void shouldNotAddProviderAnnotationWhenTransformationTriesToInjectOne() {
        // A transformation that tries to add a provider annotation where none existed
        Transformation<HasMetadata> injectingTransformation = new Transformation<>() {
            @Override
            public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata resource,
                                                            @NotNull HasItems resources,
                                                            @NotNull ReconciliationContext context) {
                return Optional.of(HasMetadata.addMetadataAnnotation(resource,
                        CoreAnnotations.JIKKOU_IO_PROVIDER, "kafka-injected"));
            }
        };

        TransformationChain chain = new TransformationChain(List.of(injectingTransformation));

        GenericResource resource = new GenericResource(
                "core/v1",
                "Test",
                ObjectMeta.builder()
                        .withName("test")
                        .build(),
                null
        );

        HasItems otherResources = ResourceList.empty();
        List<HasMetadata> result = chain.transformAll(List.of(resource), otherResources, EMPTY_CONTEXT);

        Assertions.assertEquals(1, result.size());
        Optional<String> provider = CoreAnnotations.findProviderAnnotation(result.getFirst());
        Assertions.assertTrue(provider.isEmpty(), "Provider annotation should not be present when not originally set");
    }

    @Test
    void shouldPreserveOtherAnnotationsWhileProtectingProvider() {
        // A transformation that adds a non-provider annotation
        Transformation<HasMetadata> addingTransformation = new Transformation<>() {
            @Override
            public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata resource,
                                                            @NotNull HasItems resources,
                                                            @NotNull ReconciliationContext context) {
                return Optional.of(HasMetadata.addMetadataAnnotation(resource,
                        "custom-annotation", "custom-value"));
            }
        };

        TransformationChain chain = new TransformationChain(List.of(addingTransformation));

        GenericResource resource = new GenericResource(
                "core/v1",
                "Test",
                ObjectMeta.builder()
                        .withName("test")
                        .withAnnotation(CoreAnnotations.JIKKOU_IO_PROVIDER, "kafka-prod")
                        .build(),
                null
        );

        HasItems otherResources = ResourceList.empty();
        List<HasMetadata> result = chain.transformAll(List.of(resource), otherResources, EMPTY_CONTEXT);

        Assertions.assertEquals(1, result.size());
        HasMetadata transformed = result.getFirst();
        Assertions.assertEquals("kafka-prod",
                CoreAnnotations.findProviderAnnotation(transformed).orElse(null));
        Assertions.assertEquals("custom-value",
                transformed.getMetadata().findAnnotationByKey("custom-annotation").orElse(null));
    }

    @Test
    void shouldPassThroughResourceWithoutProviderAnnotation() {
        // A transformation that adds a normal annotation
        Transformation<HasMetadata> normalTransformation = new Transformation<>() {
            @Override
            public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata resource,
                                                            @NotNull HasItems resources,
                                                            @NotNull ReconciliationContext context) {
                return Optional.of(HasMetadata.addMetadataAnnotation(resource,
                        "some-key", "some-value"));
            }
        };

        TransformationChain chain = new TransformationChain(List.of(normalTransformation));

        GenericResource resource = new GenericResource(
                "core/v1",
                "Test",
                ObjectMeta.builder().withName("test").build(),
                null
        );

        HasItems otherResources = ResourceList.empty();
        List<HasMetadata> result = chain.transformAll(List.of(resource), otherResources, EMPTY_CONTEXT);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("some-value",
                result.getFirst().getMetadata().findAnnotationByKey("some-key").orElse(null));
        Assertions.assertTrue(CoreAnnotations.findProviderAnnotation(result.getFirst()).isEmpty());
    }
}
