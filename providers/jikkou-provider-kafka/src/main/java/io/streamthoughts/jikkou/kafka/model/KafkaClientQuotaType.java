/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.model;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.jetbrains.annotations.NotNull;

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
public enum KafkaClientQuotaType {

    /**
     * Set default quotas for all users.
     * i.e.,  {@literal /config/users/<default>}
     */
    USERS_DEFAULT() {
        @Override
        public void validate(final KafkaClientQuotaEntity entity) throws JikkouRuntimeException {
            if (!isValid(toEntities(entity))) {
                throw new JikkouRuntimeException(
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
        public Map<String, String> toEntities(@NotNull final KafkaClientQuotaEntity entity) {
            Map<String, String> entities = new HashMap<>();
            entities.put(ClientQuotaEntity.USER, DEFAULT_ENTITY);
            return entities;
        }

        @Override
        public String toPettyString(final @NotNull Map<String, String> entities) {
            return String.format(
                    "%s=%s",
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
        public void validate(final KafkaClientQuotaEntity entityObject) throws JikkouRuntimeException {
            if (!isValid(toEntities(entityObject))) {
                throw new JikkouRuntimeException(
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
        public Map<String, String> toEntities(@NotNull final KafkaClientQuotaEntity entity) {
            Map<String, String> entities = new HashMap<>();
            entities.put(ClientQuotaEntity.USER, entity.getUser());
            return entities;
        }

        @Override
        public String toPettyString(final @NotNull Map<String, String> entities) {
            return String.format(
                    "%s=%s",
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
        public void validate(final KafkaClientQuotaEntity entity) throws JikkouRuntimeException {
            if (!isValid(toEntities(entity))) {
                throw new JikkouRuntimeException(
                        String.format(
                                "Defined entity for type '%s' is invalid (expected: user=<user-principal>, client-id=<client-id>)",
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
        public Map<String, String> toEntities(@NotNull final KafkaClientQuotaEntity entity) {
            Map<String, String> entities = new HashMap<>();
            entities.put(ClientQuotaEntity.USER, entity.getUser());
            entities.put(ClientQuotaEntity.CLIENT_ID, entity.getClientId());
            return entities;
        }

        @Override
        public String toPettyString(final @NotNull Map<String, String> entities) {
            return String.format(
                    "%s=%s, %s=%s",
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
        public void validate(final KafkaClientQuotaEntity entityObject) throws JikkouRuntimeException {
            if (!isValid(toEntities(entityObject))) {
                throw new JikkouRuntimeException(
                        String.format(
                                "Defined entity for type '%s' is invalid (expected: user=<user-principal>, client-id=null)",
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
        public Map<String, String> toEntities(@NotNull final KafkaClientQuotaEntity entity) {
            Map<String, String> entities = new HashMap<>();
            entities.put(ClientQuotaEntity.USER, entity.getUser());
            entities.put(ClientQuotaEntity.CLIENT_ID, DEFAULT_ENTITY);
            return entities;
        }

        @Override
        public String toPettyString(final @NotNull Map<String, String> entities) {
            return String.format(
                    "%s=%s, %s=%s",
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
        public void validate(final KafkaClientQuotaEntity entity) throws JikkouRuntimeException {
            if (!isValid(toEntities(entity))) {
                throw new JikkouRuntimeException(
                        String.format(
                                "Defined entity for type '%s' is invalid (expected: client-id=null)",
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
        public Map<String, String> toEntities(final KafkaClientQuotaEntity entity) {
            Map<String, String> entities = new HashMap<>();
            entities.put(ClientQuotaEntity.CLIENT_ID, DEFAULT_ENTITY);
            return entities;
        }

        @Override
        public String toPettyString(final @NotNull Map<String, String> entities) {
            return String.format("%s=%s", ClientQuotaEntity.CLIENT_ID, "<default>");
        }
    },

    /**
     * Set default quotas for a specific client.
     * i.e., {@literal /config/clients/<client-id>}
     */
    CLIENT() {
        @Override
        public void validate(final KafkaClientQuotaEntity entity) throws JikkouRuntimeException {
            if (!isValid(toEntities(entity))) {
                throw new JikkouRuntimeException(
                        String.format(
                                "Defined entity for type '%s' is invalid (expected: client-id=<client-id>)",
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
        public Map<String, String> toEntities(final @NotNull KafkaClientQuotaEntity entity) {
            Map<String, String> entities = new HashMap<>();
            entities.put(ClientQuotaEntity.CLIENT_ID, entity.getClientId());
            return entities;
        }

        @Override
        public String toPettyString(final @NotNull Map<String, String> entities) {
            return String.format("%s=%s", ClientQuotaEntity.CLIENT_ID, entities.get(ClientQuotaEntity.CLIENT_ID));
        }
    };

    public static final String DEFAULT_ENTITY = "";

    /**
     * Validates the given map of quota entities for this type.
     *
     * @param entityObject the {@link KafkaClientQuotaEntity} to validate.
     * @throws JikkouRuntimeException if the given map is not valid.
     */
    public abstract void validate(final KafkaClientQuotaEntity entityObject) throws JikkouRuntimeException;

    /**
     * Checks if the given map of entities is valid for this type and return the list of missing entities.
     *
     * @param entities the map of quota entities.
     * @return {@code true} if the map is valid, {@code false} otherwise.
     */
    public abstract boolean isValid(@NotNull final Map<String, String> entities);

    /**
     * Converts a given {@link KafkaClientQuotaEntity} into map of quota entities.
     *
     * @param entity the {@link KafkaClientQuotaEntity}.
     * @return the {@link Map}.
     */
    public abstract Map<String, String> toEntities(final KafkaClientQuotaEntity entity);

    /**
     * Helper method to get a {@link KafkaClientQuotaType} from the given map of entities.
     *
     * @param entries the quota entities.
     * @return the {@link KafkaClientQuotaType}.
     */
    public static KafkaClientQuotaType from(@NotNull final Map<String, String> entries) {
        for (KafkaClientQuotaType type : KafkaClientQuotaType.values()) {
            if (type.isValid(entries)) {
                return type;
            }
        }
        throw new JikkouRuntimeException("Failed to identify QuotaEntityType from: " + entries);
    }

    /**
     * Returns the petty string representation of the given entities.
     *
     * @param entity the entity to print.
     * @return the string representation of the entities.
     */
    public String toPettyString(@NotNull final KafkaClientQuotaEntity entity) {
        return toPettyString(this.toEntities(entity));
    }

    /**
     * Returns the petty string representation of the given entities.
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
