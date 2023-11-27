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