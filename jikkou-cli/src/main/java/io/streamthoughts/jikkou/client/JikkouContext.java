/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.client;

import io.streamthoughts.jikkou.api.ApiConfigurator;
import io.streamthoughts.jikkou.api.AutoApiConfigurator;
import io.streamthoughts.jikkou.api.SimpleJikkouApi;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.api.extensions.ReflectiveExtensionFactory;
import io.streamthoughts.jikkou.api.io.ResourceDeserializer;
import io.streamthoughts.jikkou.api.model.Resource;
import io.streamthoughts.jikkou.client.configure.ResourceControllerApiConfigurator;
import io.streamthoughts.jikkou.client.configure.ResourceDescriptorApiConfigurator;
import io.streamthoughts.jikkou.client.configure.ResourceTransformationApiConfigurator;
import io.streamthoughts.jikkou.client.configure.ResourceValidationApiConfigurator;
import io.streamthoughts.jikkou.kafka.LegacyKafkaClusterResourceTypeResolver;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

/**
 * Static context for Jikkou.
 */
public final class JikkouContext {

    private static JikkouConfig config;
    private static SimpleJikkouApi api;
    private static ExtensionFactory extensionFactory;

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    public static void setConfig(final @NotNull JikkouConfig config) {
        JikkouContext.config = config;
    }

    /**
     * Initializes the {@link JikkouContext} for the specified configuration.
     */
    private static void initialize() {
        if (INITIALIZED.compareAndSet(false, true)) {
            List<String> extensionPaths = JikkouConfigProperty.EXTENSION_PATHS
                    .getOptional(config)
                    .orElse(Collections.emptyList());

            // Create a new ReflectiveExtensionFactory for the user-defined extensions paths.
            extensionFactory = new ReflectiveExtensionFactory()
                .addRootApiPackage()
                .addExtensionPaths(extensionPaths);

            // Register all the resource extensions
            extensionFactory
                .allExtensionsDescriptorForType(Resource.class)
                .forEach(descriptor -> ResourceDeserializer.registerKind(descriptor.clazz()));

            ResourceDeserializer.registerResolverType(new LegacyKafkaClusterResourceTypeResolver());

            // Create Jikkou API
            api = ApiConfigurator.emptyList()
                    .with(new AutoApiConfigurator(extensionFactory))
                    .with(new ResourceDescriptorApiConfigurator(extensionFactory))
                    .with(new ResourceControllerApiConfigurator(extensionFactory))
                    .with(new ResourceValidationApiConfigurator(extensionFactory))
                    .with(new ResourceTransformationApiConfigurator(extensionFactory))
                    .configure(SimpleJikkouApi.builder(), config)
                    .build();
        } else {
            throw new IllegalStateException("JikkouContext cannot be initialized twice.");
        }
    }

    /**
     * Gets the static {@link ExtensionFactory}.
     *
     * @return  the {@link ExtensionFactory}.
     */
    public static ExtensionFactory extensionFactory() {
        checkState();
        return JikkouContext.extensionFactory;
    }

    /**
     * Gets the static {@link JikkouConfig}.
     *
     * @return  the {@link JikkouConfig}.
     */
    public static JikkouConfig jikkouConfig() {
        return JikkouContext.config;
    }

    /**
     * Gets the static {@link SimpleJikkouApi}.
     *
     * @return  the {@link SimpleJikkouApi}.
     */
    public static SimpleJikkouApi jikkouApi() {
        checkState();
        return JikkouContext.api;
    }

    private static void checkState() {
        if (!INITIALIZED.get()) {
            JikkouContext.initialize();
        }
    }
}
