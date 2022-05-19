/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.api.model;

import io.streamthoughts.jikkou.api.error.JikkouException;
import io.streamthoughts.jikkou.api.error.ExecutionException;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Supported entity-types for altering quotas.
 *
 * <pre>
 * The order of precedence for quota configuration is:
 *  1. /config/users/:user/clients/:client-id
 *  2. /config/users/:user/clients/default
 *  3. /config/users/:user
 *  4. /config/users/default/clients/:client-id
 *  5. /config/users/default/clients/default
 *  6. /config/users/default
 *  7. /config/clients/:client-id
 *  8. /config/clients/default
 * </pre>
 */
public enum V1QuotaType {

    /**
     * Set default quotas for all users.
     * i.e.,  {@literal /config/users/<default>}
     */
    USERS_DEFAULT() {
        @Override
        public void validate(final V1QuotaEntityObject entityObject) throws JikkouException {
            if (!isValid(toEntities(entityObject))) {
                throw new JikkouException(
                        String.format(
                                "Defined entity for type '%s' is invalid (expected: user=null)",
                                this.name()
                        ));
            }
        }

        @Override
        public boolean isValid(@NotNull final Map<String, String> entities) {
            return entities.size() == 1 &&
                    isDefault(entities, ClientQuotaEntity.USER);
        }

        @Override
        public Map<String, String> toEntities(@NotNull final V1QuotaEntityObject entityObject) {
            return new HashMap<>() {{
                put(ClientQuotaEntity.USER, DEFAULT);
            }};
        }

        @Override
        public String toPettyString(final @NotNull Map<String, String> entities) {
            return String.format(
                    "(%s=%s)",
                    ClientQuotaEntity.USER,
                    "<default>"
            );
        }
    },

    /**
     * Set quotas for a specific user principal.
     * i.e.,  {@literal /config/users/<user>}
     */
    USER() {
        @Override
        public void validate(final V1QuotaEntityObject entityObject) throws JikkouException {
            if (!isValid(toEntities(entityObject))) {
                throw new JikkouException(
                        String.format(
                                "Defined entity for type '%s' is invalid (expected: user=<user-principal>)",
                                this.name()
                        )
                );
            }
        }

        @Override
        public boolean isValid(@NotNull final Map<String, String> entities) {
            return entities.size() == 1 &&
                    isNotDefault(entities, ClientQuotaEntity.USER);
        }

        @Override
        public Map<String, String> toEntities(@NotNull final V1QuotaEntityObject entityObject) {
            return new HashMap<>() {{
                put(ClientQuotaEntity.USER, entityObject.user());
            }};
        }

        @Override
        public String toPettyString(final @NotNull Map<String, String> entities) {
            return String.format(
                    "(%s=%s)",
                    ClientQuotaEntity.USER,
                    entities.get(ClientQuotaEntity.USER)
            );
        }
    },

    /**
     * Set quotas for a specific user principal and a specific client-id.
     * i.e., {@literal config/users/<user>/clients/<client-id> }.
     */
    USER_CLIENT() {
        @Override
        public void validate(final V1QuotaEntityObject entityObject) throws JikkouException {
            if (!isValid(toEntities(entityObject))) {
                throw new JikkouException(
                        String.format(
                                "Defined entity for type '%s' is invalid (expected: user=<user-principal>, client_client=<client-id>)",
                                this.name()
                        )
                );
            }
        }

        @Override
        public boolean isValid(@NotNull Map<String, String> entities) {
            return entities.size() == 2 &&
                    isNotDefault(entities, ClientQuotaEntity.USER) &&
                    isNotDefault(entities, ClientQuotaEntity.CLIENT_ID);
        }

        @Override
        public Map<String, String> toEntities(@NotNull final V1QuotaEntityObject entityObject) {
            return new HashMap<>() {{
                put(ClientQuotaEntity.USER, entityObject.user());
                put(ClientQuotaEntity.CLIENT_ID, entityObject.clientId());
            }};
        }

        @Override
        public String toPettyString(final @NotNull Map<String, String> entities) {
            return String.format(
                    "(%s=%s, %s=%s)",
                    ClientQuotaEntity.USER,
                    entities.get(ClientQuotaEntity.USER),
                    ClientQuotaEntity.CLIENT_ID,
                    entities.get(ClientQuotaEntity.CLIENT_ID)
            );
        }
    },

    /**
     * Set default quotas for a specific user and all clients
     * i.e., {@literal /config/users/<user>/clients/<default>}
     */
    USER_ALL_CLIENTS() {
        @Override
        public void validate(final V1QuotaEntityObject entityObject) throws JikkouException {
            if (!isValid(toEntities(entityObject))) {
                throw new JikkouException(
                        String.format(
                                "Defined entity for type '%s' is invalid (expected: user=<user-principal>, client_client=null)",
                                this.name()
                        )
                );
            }
        }

        @Override
        public boolean isValid(@NotNull final Map<String, String> entities) {
            return entities.size() == 2 &&
                    isNotDefault(entities, ClientQuotaEntity.USER) &&
                    isDefault(entities, ClientQuotaEntity.CLIENT_ID);
        }

        @Override
        public Map<String, String> toEntities(@NotNull final V1QuotaEntityObject entityObject) {
            return new HashMap<>() {{
                put(ClientQuotaEntity.USER, entityObject.user());
                put(ClientQuotaEntity.CLIENT_ID, DEFAULT);
            }};
        }

        @Override
        public String toPettyString(final @NotNull Map<String, String> entities) {
            return String.format(
                    "(%s=%s, %s=%s)",
                    ClientQuotaEntity.USER,
                    entities.get(ClientQuotaEntity.USER),
                    ClientQuotaEntity.CLIENT_ID,
                    "<default>"
            );
        }
    },

    /**
     * Set default quotas for all clients
     * i.e., {@literal /config/clients/<default> }
     */
    CLIENTS_DEFAULT() {
        @Override
        public void validate(final V1QuotaEntityObject entityObject) throws JikkouException {
            if (!isValid(toEntities(entityObject))) {
                throw new JikkouException(
                        String.format(
                                "Defined entity for type '%s' is invalid (expected: client_client=null)",
                                this.name()
                        )
                );
            }
        }

        @Override
        public boolean isValid(@NotNull final Map<String, String> entities) {
            return entities.size() == 1 &&
                    isDefault(entities, ClientQuotaEntity.CLIENT_ID);
        }

        @Override
        public Map<String, String> toEntities(final V1QuotaEntityObject entityObject) {
            return new HashMap<>() {{
                put(ClientQuotaEntity.CLIENT_ID, DEFAULT);
            }};
        }

        @Override
        public String toPettyString(final @NotNull Map<String, String> entities) {
            return String.format("(%s=%s)", ClientQuotaEntity.CLIENT_ID, "<default>");
        }
    },

    /**
     * Set default quotas for a specific client.
     * i.e., {@literal /config/clients/<client-id>}
     */
    CLIENT() {
        @Override
        public void validate(final V1QuotaEntityObject entityObject) throws JikkouException {
            if (!isValid(toEntities(entityObject))) {
                throw new JikkouException(
                        String.format(
                                "Defined entity for type '%s' is invalid (expected: client_client=<client-id>)",
                                this.name()
                        )
                );
            }
        }

        @Override
        public boolean isValid(final @NotNull Map<String, String> entities) {
            return entities.size() == 1 &&
                    isNotDefault(entities, ClientQuotaEntity.CLIENT_ID);
        }

        @Override
        public Map<String, String> toEntities(final @NotNull V1QuotaEntityObject entityObject) {
            return new HashMap<>() {{
                put(ClientQuotaEntity.CLIENT_ID, entityObject.clientId());
            }};
        }

        @Override
        public String toPettyString(final @NotNull Map<String, String> entities) {
            return String.format("(%s=%s)", ClientQuotaEntity.CLIENT_ID, entities.get(ClientQuotaEntity.CLIENT_ID));
        }
    };

    public static final String DEFAULT = null;

    /**
     * Validates the given map of quota entities for this type.
     *
     * @param entityObject the {@link V1QuotaEntityObject} to validate.
     * @throws JikkouException if the given map is not valid.
     */
    public abstract void validate(final V1QuotaEntityObject entityObject) throws JikkouException;

    /**
     * Checks if the given map of entities is valid for this type and return the list of missing entities.
     *
     * @param entities the map of quota entities.
     * @return {@code true} if the map is valid, {@code false} otherwise.
     */
    public abstract boolean isValid(@NotNull final Map<String, String> entities);

    /**
     * Converts a given {@link V1QuotaEntityObject} into map of quota entities.
     *
     * @param entityObject the {@link V1QuotaEntityObject}.
     * @return the {@link Map}.
     */
    public abstract Map<String, String> toEntities(final V1QuotaEntityObject entityObject);

    /**
     * Helper method to get a {@link V1QuotaType} from the given map of entities.
     *
     * @param entries the quota entities.
     * @return the {@link V1QuotaType}.
     */
    public static V1QuotaType from(@NotNull final Map<String, String> entries) {
        for (V1QuotaType type : V1QuotaType.values()) {
            if (type.isValid(entries)) {
                return type;
            }
        }
        throw new ExecutionException("Failed to identify QuotaEntityType from: " + entries);
    }

    /**
     * Returns the a petty string representation of the given entities.
     *
     * @param entity the entity to print.
     * @return the string representation of the entities.
     */
    public String toPettyString(@NotNull final V1QuotaEntityObject entity) {
        return toPettyString(this.toEntities(entity));
    }

    /**
     * Returns the a petty string representation of the given entities.
     *
     * @param entities the entities to print.
     * @return the string representation of the entities.
     */
    public abstract String toPettyString(@NotNull final Map<String, String> entities);

    private static boolean isNotDefault(@NotNull final Map<String, String> entities, final String entityType) {
        return entities.containsKey(entityType) && entities.get(entityType) != null;
    }

    private static boolean isDefault(@NotNull final Map<String, String> entities, final String entityType) {
        return entities.containsKey(entityType) && entities.get(entityType) == null;
    }
}
