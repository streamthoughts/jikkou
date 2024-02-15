/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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