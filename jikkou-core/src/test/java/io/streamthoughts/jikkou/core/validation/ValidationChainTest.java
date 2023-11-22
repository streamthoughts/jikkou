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