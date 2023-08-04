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
package io.streamthoughts.jikkou.api.validation;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.TestResource;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.model.ResourceType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ResourceValidationChainTest {


    @Test
    void shouldNotRunValidationForResourceAnnotatedWithByPassTrue() {
        // Given
        ResourceValidation mkValidation = Mockito.mock(ResourceValidation.class);
        TestResource resource = new TestResource()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_BYPASS_VALIDATIONS, true)
                        .build()
                );
        // When
        ResourceValidationChain chain = new ResourceValidationChain(List.of(mkValidation));

        chain.validate(List.of(resource));

        // Then
        Mockito.verify(mkValidation, Mockito.never()).validate(Mockito.anyList());
    }

    @Test
    void shouldRunValidationForResourceAnnotatedWithByPassFalse() {
        // Given
        ResourceValidation mkValidation = Mockito.mock(ResourceValidation.class);
        Mockito.when(mkValidation.canAccept(Mockito.any(ResourceType.class)))
                .thenReturn(true);

        TestResource resource = new TestResource()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_BYPASS_VALIDATIONS, false)
                        .build()
                );
        // When
        ResourceValidationChain chain = new ResourceValidationChain(List.of(mkValidation));

        chain.validate(List.of(resource));

        // Then
        Mockito.verify(mkValidation, Mockito.times(1)).validate(Mockito.anyList());
    }
}