/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api;

import io.streamthoughts.jikkou.extension.aiven.api.data.CompatibilityCheckResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaQuotaEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicInfoCreate;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicInfoResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicInfoUpdate;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicListResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.ListKafkaAclResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.ListKafkaQuotaResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.ListSchemaRegistryAclResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.ListSchemaSubjectsResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.MessageErrorsResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.ServiceInformationResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.SubjectSchemaConfigurationResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.SubjectSchemaRegistrationResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.SubjectSchemaVersionResponse;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityObject;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Aiven - REST API Client.
 */
public final class AivenApiClient implements AutoCloseable {

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
     * Get service information
     *
     * @see AivenApi#getServiceInformation(String, String)
     */
    public ServiceInformationResponse getServiceInformation() {
        return this.api.getServiceInformation(project, service);
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
     * List Kafka quota
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
     * List Schema Registry Subjects.
     *
     * @see AivenApi#listSchemaRegistrySubjects(String, String).
     */
    public ListSchemaSubjectsResponse listSchemaRegistrySubjects() {
        return this.api.listSchemaRegistrySubjects(project, service);
    }

    /**
     * Gets Schema Registry Subject Version.
     *
     * @see AivenApi#getSchemaRegistrySubjectByVersionId(String, String, String, String)
     */
    public SubjectSchemaVersionResponse getSchemaRegistrySubjectByVersionId(final @NotNull String subject,
                                                                            final @NotNull String versionId) {

        return this.api.getSchemaRegistrySubjectByVersionId(project, service, subject, versionId);
    }

    /**
     * Gets Schema Registry Latest Subject Version.
     *
     * @see AivenApi#getSchemaRegistrySubjectByVersionId(String, String, String, String)
     */
    public SubjectSchemaVersionResponse getSchemaRegistryLatestSubjectVersion(final @NotNull String subject) {

        return this.api.getSchemaRegistrySubjectByVersionId(project, service, subject, "latest");
    }


    /**
     * Register Schema Registry Subject Version.
     *
     * @param subject the subject.
     * @param schema  the subject schema to register.
     */
    public SubjectSchemaRegistrationResponse registerSchemaRegistrySubjectVersion(final @NotNull String subject,
                                                                                  final @NotNull SubjectSchemaRegistration schema) {
        return this.api.registerSchemaRegistrySubjectVersion(project, service, subject, schema);
    }

    /**
     * Delete Schema Registry Subject Version.
     *
     * @param subject Schema Subject
     */
    public MessageErrorsResponse deleteSchemaRegistrySubject(final @NotNull String subject) {
        return this.api.deleteSchemaRegistrySubject(project, service, subject);
    }

    /**
     * Get the global configuration for Schema Registry.
     */
    public SubjectSchemaConfigurationResponse getSchemaRegistryGlobalCompatibility() {
        return this.api.getSchemaRegistryGlobalCompatibility(project, service);
    }

    /**
     * Get configuration for Schema Registry subject.
     *
     * @param subject Schema Subject
     */
    public SubjectSchemaConfigurationResponse getSchemaRegistrySubjectCompatibility(final @NotNull String subject) {
        return this.api.getSchemaRegistrySubjectCompatibility(project, service, subject);
    }

    /**
     * Update configuration for Schema Registry subject
     *
     * @param subject       Schema Subject
     * @param compatibility Schema Subject Compatibility.
     */
    public MessageErrorsResponse updateSchemaRegistrySubjectCompatibility(final @NotNull String subject,
                                                                          final @NotNull CompatibilityObject compatibility) {
        return this.api.updateSchemaRegistrySubjectCompatibility(project, service, subject, compatibility);
    }

    /**
     * Check compatibility of schema in Schema Registry
     *
     * @param subject   Schema Subject
     * @param versionId Subject Version ID
     */
    public CompatibilityCheckResponse checkSchemaRegistryCompatibility(final @NotNull String subject,
                                                                       final @NotNull String versionId,
                                                                       final @NotNull SubjectSchemaRegistration schema) {
        return this.api.checkSchemaRegistryCompatibility(project, service, subject, versionId, schema);
    }

    /**
     * Get Kafka topic list.
     *
     * @return the {@link KafkaTopicListResponse}.
     */
    public KafkaTopicListResponse listKafkaTopics() {
        return this.api.getKafkaTopicList(project, service);
    }

    /**
     * Get Kafka topic info.
     *
     * @return the {@link KafkaTopicListResponse}.
     */
    public KafkaTopicInfoResponse getKafkaTopicInfo(final @NotNull String topic) {
        return this.api.getKafkaTopicInfo(project, service, topic);
    }

    /**
     * Update Kafka topic info.
     *
     * @return the {@link KafkaTopicListResponse}.
     */
    public MessageErrorsResponse createKafkaTopicInfo(final KafkaTopicInfoCreate payload) {
        return this.api.createKafkaTopicInfo(project, service, payload);
    }

    /**
     * Update Kafka topic info.
     *
     * @return the {@link KafkaTopicListResponse}.
     */
    public MessageErrorsResponse updateKafkaTopicInfo(final @NotNull String topic,
                                                      final KafkaTopicInfoUpdate payload) {
        return this.api.updateKafkaTopicInfo(project, service, topic, payload);
    }

    /**
     * Delete Kafka topic.
     *
     * @return the {@link KafkaTopicListResponse}.
     */
    public MessageErrorsResponse deleteKafkaTopicInfo(final @NotNull String topic) {
        return this.api.deleteKafkaTopicInfo(project, service, topic);
    }


    /**
     * {@inheritDoc}
     **/
    @Override
    public void close() {
        this.api.close();
    }
}
