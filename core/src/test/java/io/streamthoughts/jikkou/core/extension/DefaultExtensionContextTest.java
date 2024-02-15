/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.ConfigPropertySpec;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.builder.ExtensionDescriptorBuilder;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultExtensionContextTest {

    @Test
    void shouldGetConfigPropertiesFromDescriptorForEnum() {
        List<ConfigProperty> properties = DefaultExtensionContext.getConfigProperties(ExtensionDescriptorBuilder
                .builder()
                .type(Object.class)
                .supplier(Object::new)
                .properties(List.of(new ConfigPropertySpec(
                        "prop",
                        TestEnum.class,
                        "",
                        null,
                        false
                )))
                .build()
        );
        Assertions.assertNotNull(properties);
        Assertions.assertEquals(TestEnum.VALUE, properties.get(0).get(Configuration.of("prop", "VALUE")));
    }

    private enum TestEnum {
        VALUE
    }
}