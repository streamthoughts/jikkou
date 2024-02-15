/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals.admin;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAdminClientFactory implements AdminClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAdminClientFactory.class);

    private final Supplier<Map<String, Object>> clientPropertiesSupplier;

    /**
     * Creates a new {@link DefaultAdminClientFactory} instance.
     * @param clientProperties  the client's properties.
     */
    public DefaultAdminClientFactory(@NotNull Map<String, Object> clientProperties) {
        Objects.requireNonNull(
                clientProperties,
                "clientProperties must not be null"
        );
        Map<String, Object> immutable = Collections.unmodifiableMap(clientProperties);
        this.clientPropertiesSupplier = () -> immutable;
    }

    /**
     * Creates a new {@link DefaultAdminClientFactory} instance.
     * @param clientPropertiesSupplier  the client's properties supplier.
     */
    public DefaultAdminClientFactory(@NotNull Supplier<Map<String, Object>> clientPropertiesSupplier) {
        this.clientPropertiesSupplier = Objects.requireNonNull(
                clientPropertiesSupplier,
                "clientPropertiesSupplier must not be null"
        );
    }

    /** {@inheritDoc} **/
    @Override
    public AdminClient createAdminClient() {
        return AdminClient.create(clientPropertiesSupplier.get());
    }
}
