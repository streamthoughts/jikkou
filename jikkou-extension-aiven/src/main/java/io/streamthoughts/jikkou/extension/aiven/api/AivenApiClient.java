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
package io.streamthoughts.jikkou.extension.aiven.api;

import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaQuotaEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.ListKafkaAclResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.ListKafkaQuotaResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.ListSchemaRegistryAclResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.MessageErrorsResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class AivenApiClient implements AutoCloseable {

    private final AivenApi api;

    private final String project;

    private final String service;

    /**
     * Creates a new {@link AivenApiClient} instance.
     *
     * @param api     the REST API.
     * @param project the project name.
     * @param service the service name.
     */
    public AivenApiClient(final @NotNull AivenApi api,
                          final @NotNull String project,
                          final @NotNull String service) {
        this.api = Objects.requireNonNull(api, "api must not be null");
        this.project = Objects.requireNonNull(project, "project must not be null");
        this.service = Objects.requireNonNull(service, "service must not be null");
    }

    /**
     * Add a Kafka ACL entry.
     *
     * @param entry Kafka ACL entry
     * @see AivenApi#addKafkaAclEntry(String, String, KafkaAclEntry)
     */
    public ListKafkaAclResponse addKafkaAclEntry(final KafkaAclEntry entry) {
        return this.api.addKafkaAclEntry(project, service, entry);
    }

    /**
     * List Kafka ACL entries
     *
     * @see AivenApi#listKafkaAclEntries(String, String)
     */
    public ListKafkaAclResponse listKafkaAclEntries() {
        return this.api.listKafkaAclEntries(project, service);
    }

    /**
     * Delete Kafka ACL entry.
     *
     * @see AivenApi#deleteKafkaAclEntry(String, String, String)
     */
    public ListKafkaAclResponse deleteKafkaAclEntry(final String id) {
        return this.api.deleteKafkaAclEntry(project, service, id);
    }

    /**
     * Add a Schema Registry ACL entries
     *
     * @param entry Kafka ACL entry
     * @see AivenApi#addSchemaRegistryAclEntry(String, String, SchemaRegistryAclEntry)
     */
    public ListSchemaRegistryAclResponse addSchemaRegistryAclEntry(final SchemaRegistryAclEntry entry) {
        return this.api.addSchemaRegistryAclEntry(project, service, entry);
    }

    /**
     * List Schema Registry ACL entries
     *
     * @see AivenApi#listSchemaRegistryAclEntries(String, String)
     */
    public ListSchemaRegistryAclResponse listSchemaRegistryAclEntries() {
        return this.api.listSchemaRegistryAclEntries(project, service);
    }

    /**
     * Delete Kafka ACL entry.
     *
     * @see AivenApi#deleteSchemaRegistryAclEntry(String, String, String)
     */
    public ListSchemaRegistryAclResponse deleteSchemaRegistryAclEntry(final String id) {
        return this.api.deleteSchemaRegistryAclEntry(project, service, id);
    }

    /**
     * Create Kafka quota
     *
     * @param entry Kafka ACL entry
     * @see AivenApi#addSchemaRegistryAclEntry(String, String, SchemaRegistryAclEntry)
     */
    public MessageErrorsResponse createKafkaQuota(final KafkaQuotaEntry entry) {
        return this.api.createKafkaQuota(project, service, entry);
    }

    /**
     * List Kfka quota
     *
     * @see AivenApi#listSchemaRegistryAclEntries(String, String)
     */
    public ListKafkaQuotaResponse listKafkaQuotas() {
        return this.api.listKafkaQuotas(project, service);
    }

    /**
     * Delete Kafka quota.
     *
     * @see AivenApi#deleteKafkaQuota(String, String, String, String) (String, String, KafkaQuotaEntry)
     */
    public MessageErrorsResponse deleteKafkaQuota(final KafkaQuotaEntry entry) {
        return this.api.deleteKafkaQuota(project, service, entry.clientId(), entry.user());
    }


    /**
     * {@inheritDoc}
     **/
    @Override
    public void close() {
        this.api.close();
    }
}
