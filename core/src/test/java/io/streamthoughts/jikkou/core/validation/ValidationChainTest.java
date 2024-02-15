/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.validation;

import io.streamthoughts.jikkou.core.TestResource;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ValidationChainTest {


    @Test
    void shouldNotRunValidationForResourceAnnotatedWithByPassTrue() {
        // Given
        Validation mkValidation = Mockito.mock(Validation.class);
        TestResource resource = new TestResource()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation(CoreAnnotations.JIKKOU_BYPASS_VALIDATIONS, true)
                        .build()
                );
        // When
        ValidationChain chain = new ValidationChain(List.of(mkValidation));

        chain.validate(List.of(resource));

        // Then
        Mockito.verify(mkValidation, Mockito.never()).validate(Mockito.anyList());
    }

    @Test
    void shouldRunValidationForResourceAnnotatedWithByPassFalse() {
        // Given
        Validation<HasMetadata> validation = Mockito.spy(new Validation<>() {});

        Mockito.when(validation.canAccept(Mockito.any(ResourceType.class)))
                .thenReturn(true);

        TestResource resource = new TestResource()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation(CoreAnnotations.JIKKOU_BYPASS_VALIDATIONS, false)
                        .build()
                );
        // When
        ValidationChain chain = new ValidationChain(List.of(validation));

        chain.validate(List.of(resource));

        // Then
        Mockito.verify(validation, Mockito.times(1)).validate(Mockito.anyList());
    }
}