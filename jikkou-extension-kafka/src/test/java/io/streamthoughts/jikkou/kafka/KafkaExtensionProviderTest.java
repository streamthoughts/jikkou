/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka;

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.extensions.DefaultExtensionFactory;
import io.streamthoughts.jikkou.api.extensions.ExtensionDescriptor;
import io.streamthoughts.jikkou.api.transform.ResourceTransformation;
import io.streamthoughts.jikkou.api.validation.ResourceValidation;
import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaExtensionProviderTest {

    @Test
    void shouldRegisterValidationsThatAcceptAtLeastOneResource() {
        // Given
        KafkaExtensionProvider provider = new KafkaExtensionProvider();
        DefaultExtensionFactory factory = new DefaultExtensionFactory();

        // When
        provider.registerExtensions(factory, Configuration.empty());

        // Then
        Collection<ExtensionDescriptor<ResourceValidation>> descriptors = factory
                .getAllDescriptorsForType(ResourceValidation.class);

        for (ExtensionDescriptor<ResourceValidation> descriptor : descriptors) {
            Assertions.assertFalse(
                    descriptor.getSupportedResources().isEmpty(),
                    "Class '" + descriptor.classType() + "' does not support any resource");
        }
    }

    @Test
    void shouldRegisterTransformationsThatAcceptAtLeastOneResource() {
        // Given
        KafkaExtensionProvider provider = new KafkaExtensionProvider();
        DefaultExtensionFactory factory = new DefaultExtensionFactory();

        // When
        provider.registerExtensions(factory, Configuration.empty());

        // Then
        Collection<ExtensionDescriptor<ResourceTransformation>> descriptors = factory
                .getAllDescriptorsForType(ResourceTransformation.class);

        for (ExtensionDescriptor<ResourceTransformation> descriptor : descriptors) {
            Assertions.assertFalse(
                    descriptor.getSupportedResources().isEmpty(),
                    "Class '" + descriptor.classType() + "' does not support any resource");
        }
    }

}