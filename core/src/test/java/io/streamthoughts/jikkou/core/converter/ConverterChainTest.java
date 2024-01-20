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
package io.streamthoughts.jikkou.core.converter;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.generics.GenericResource;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ConverterChainTest {

    @Test
    void shouldApplyAllMatchingConverters() {
        // Given
        IdentityConverter converter1 = Mockito.spy(new IdentityConverter());
        IdentityConverter converter2 = Mockito.spy(new IdentityConverter());
        ConverterChain chain = new ConverterChain(List.of(converter1, converter2));
        HasMetadata resource = new GenericResource("", "", new ObjectMeta(), null);

        // When
        List<HasMetadata> result = chain.apply(resource);

        // Then
        Assertions.assertEquals(List.of(resource), result);
        Mockito.verify(converter1, Mockito.times(1)).apply(Mockito.eq(resource));
        Mockito.verify(converter2, Mockito.times(1)).apply(Mockito.eq(resource));
    }

}