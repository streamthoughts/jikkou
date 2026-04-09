/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.reconciler.config;

import io.jikkou.core.config.ConfigPropertySpec;
import io.jikkou.core.extension.ExtensionDescriptor;
import io.jikkou.core.extension.builder.ExtensionDescriptorBuilder;
import io.jikkou.core.models.ApiOptionSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApiOptionSpecFactoryTest {

    private final ApiOptionSpecFactory factory = new ApiOptionSpecFactory();

    @Test
    void shouldCreateApiResourceVerbOptionListFromExtension() {
        ExtensionDescriptor<Object> descriptor = ExtensionDescriptorBuilder.builder()
                .type(Object.class)
                .supplier(Object::new)
                .properties(List.of(new ConfigPropertySpec(
                        "Test",
                        String.class,
                        "Description",
                        "default",
                        false
                )))
                .build();
        List<ApiOptionSpec> result = factory.make(descriptor);
        Assertions.assertEquals(
                List.of(new ApiOptionSpec(
                        "Test",
                        "Description",
                        String.class,
                        "default",
                        false)
                ),
                result
        );
    }
}