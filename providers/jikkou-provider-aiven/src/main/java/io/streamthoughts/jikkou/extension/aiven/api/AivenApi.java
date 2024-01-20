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

import io.streamthoughts.jikkou.extension.aiven.api.data.CompatibilityCheckResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaQuotaEntry;
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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.jetbrains.annotations.NotNull;

/**
 * REST API for Aiven (see <a href="https://api.aiven.io/doc/">https://api.aiven.io/doc/</a>)
 */
@Path("/project/{project}/service/{service_name}")
@Produces("application/json")
public interface AivenApi extends AutoCloseable {

    /*
     * ----------------------------------------------------------------------------------------------------------------
     * SERVICE
     * ----------------------------------------------------------------------------------------------------------------
     */
    /**
     * Get service information
     *
     * @param project Project name
     * @param service Service name
     */
    @GET
    @Path("")
    ServiceInformationResponse getServiceInformation(@PathParam("project") String project,
                                                     @PathParam("service_name") String service);

    /*
     * ----------------------------------------------------------------------------------------------------------------
     * KAFKA - ACLs
     * ----------------------------------------------------------------------------------------------------------------
     */

    /**
     * Add a Kafka ACL entry.
     *
     * @param project Project name
     * @param service Service name
     * @param entry   Kafka ACL entry
     */
    @POST
    @Path("/acl")
    @Consumes("application/json")
    ListKafkaAclResponse addKafkaAclEntry(@PathParam("project") String project,
                                          @PathParam("service_name") String service,
                                          KafkaAclEntry entry);

    /**
     * List Kafka ACL entries
     *
     * @param project Project name
     * @param service Service name
     */
    @GET
    @Path("/acl")
    ListKafkaAclResponse listKafkaAclEntries(@PathParam("project") String project,
                                             @PathParam("service_name") String service);

    /**
     * Delete Kafka ACL entry.
     *
     * @param project Project name
     * @param service Service name
     * @param id      Kafka ACL Entry ID
     */
    @DELETE
    @Path("/acl/{kafka_acl_id}")
    ListKafkaAclResponse deleteKafkaAclEntry(@PathParam("project") String project,
                                             @PathParam("service_name") String service,
                                             @PathParam("kafka_acl_id") String id);

    /*
     * ----------------------------------------------------------------------------------------------------------------
     * KAFKA - SCHEMA REGISTRY - ACLs
     * ----------------------------------------------------------------------------------------------------------------
     */

    /**
     * List Schema Registry ACL entries
     *
     * @param project Project name
     * @param service Service name
     */
    @GET
    @Path("/kafka/schema-registry/acl")
    ListSchemaRegistryAclResponse listSchemaRegistryAclEntries(@PathParam("project") String project,
                                                               @PathParam("service_name") String service);

    /**
     * Add a Schema Registry ACL entries
     *
     * @param project Project name
     * @param service Service name
     * @param entry   Schema Registry ACL entries
     */
    @POST
    @Path("/kafka/schema-registry/acl")
    @Consumes("application/json")
    ListSchemaRegistryAclResponse addSchemaRegistryAclEntry(@PathParam("project") String project,
                                                            @PathParam("service_name") String service,
                                                            SchemaRegistryAclEntry entry);

    /**
     * Delete Schema Registry ACL entries
     *
     * @param project Project name
     * @param service Service name
     * @param id      Kafka ACL Entry ID
     */
    @DELETE
    @Path("/kafka/schema-registry/acl/{schema_registry_acl_id}")
    ListSchemaRegistryAclResponse deleteSchemaRegistryAclEntry(@PathParam("project") String project,
                                                               @PathParam("service_name") String service,
                                                               @PathParam("schema_registry_acl_id") String id);

    /*
     * ----------------------------------------------------------------------------------------------------------------
     * KAFKA - QUOTAS
     * ----------------------------------------------------------------------------------------------------------------
     */

    /**
     * List Kafka quotas
     *
     * @param project Project name
     * @param service Service name
     */
    @GET
    @Path("/quota")
    ListKafkaQuotaResponse listKafkaQuotas(@PathParam("project") String project,
                                           @PathParam("service_name") String service);

    /**
     * Create Kafka quota
     *
     * @param project Project name
     * @param service Service name
     */
    @POST
    @Path("/quota")
    @Consumes("application/json")
    MessageErrorsResponse createKafkaQuota(@PathParam("project") String project,
                                           @PathParam("service_name") String service,
                                           KafkaQuotaEntry entry);

    /**
     * Delete Kafka quota
     *
     * @param project Project name
     * @param service Service name
     */
    @DELETE
    @Path("/quota")
    @Consumes("application/json")
    MessageErrorsResponse deleteKafkaQuota(@PathParam("project") String project,
                                           @PathParam("service_name") String service,
                                           @QueryParam("client-id") String client,
                                           @QueryParam("user") String user);
    /*
     * ----------------------------------------------------------------------------------------------------------------
     * SCHEMA REGISTRY - SUBJECTS
     * ----------------------------------------------------------------------------------------------------------------
     */

    /**
     * List Schema Registry Subjects.
     *
     * @param project Project name
     * @param service Service name
     */
    @GET
    @Path("/kafka/schema/subjects")
    ListSchemaSubjectsResponse listSchemaRegistrySubjects(@PathParam("project") String project,
                                                          @PathParam("service_name") String service);

    /**
     * Gets Schema Registry Subject Version.
     *
     * @param project Project name
     * @param service Service name
     */
    @GET
    @Path("/kafka/schema/subjects/{subject_name}/versions/{version_id}")
    SubjectSchemaVersionResponse getSchemaRegistrySubjectByVersionId(@PathParam("project") String project,
                                                                     @PathParam("service_name") String service,
                                                                     @PathParam("subject_name") String subject,
                                                                     @PathParam("version_id") String versionId);

    /**
     * Register Schema Registry Subject Version.
     *
     * @param project Project name
     * @param service Service name
     * @param subject Schema Subject
     */
    @POST
    @Path("/kafka/schema/subjects/{subject_name}/versions")
    @Consumes("application/json")
    SubjectSchemaRegistrationResponse registerSchemaRegistrySubjectVersion(@PathParam("project") String project,
                                                                           @PathParam("service_name") String service,
                                                                           @PathParam("subject_name") String subject,
                                                                           SubjectSchemaRegistration schema);

    /**
     * Delete Schema Registry Subject Version.
     *
     * @param project Project name
     * @param service Service name
     * @param subject Schema Subject
     */
    @DELETE
    @Path("/kafka/schema/subjects/{subject_name}")
    MessageErrorsResponse deleteSchemaRegistrySubject(@PathParam("project") String project,
                                                      @PathParam("service_name") String service,
                                                      @PathParam("subject_name") String subject);

    /*
     * ----------------------------------------------------------------------------------------------------------------
     * SCHEMA REGISTRY - COMPATIBILITY
     * ----------------------------------------------------------------------------------------------------------------
     */
    /**
     * Get configuration for Schema Registry subject
     *
     * @param project Project name
     * @param service Service name
     */
    @GET
    @Path("/kafka/schema/config")
    SubjectSchemaConfigurationResponse getSchemaRegistryGlobalCompatibility(@PathParam("project") String project,
                                                                            @PathParam("service_name") String service);

    /**
     * Get configuration for Schema Registry subject
     *
     * @param project Project name
     * @param service Service name
     * @param subject Schema Subject
     */
    @GET
    @Path("/kafka/schema/config/{subject_name}")
    SubjectSchemaConfigurationResponse getSchemaRegistrySubjectCompatibility(@PathParam("project") String project,
                                                                             @PathParam("service_name") String service,
                                                                             @PathParam("subject_name") String subject);

    /**
     * Update configuration for Schema Registry subject
     *
     * @param project Project name
     * @param service Service name
     * @param subject Schema Subject
     */
    @PUT
    @Path("/kafka/schema/config/{subject_name}")
    @Consumes("application/json")
    MessageErrorsResponse updateSchemaRegistrySubjectCompatibility(@PathParam("project") String project,
                                                                   @PathParam("service_name") String service,
                                                                   @PathParam("subject_name") String subject,
                                                                   CompatibilityObject compatibilityObject);

    /**
     * Check compatibility of schema in Schema Registry
     *
     * @param project   Project name
     * @param service   Service name
     * @param subject   Schema Subject
     * @param versionId Subject Version ID
     */
    @POST
    @Path("/kafka/schema/compatibility/subjects/{subject_name}/versions/{version_id}")
    @Consumes("application/json")
    CompatibilityCheckResponse checkSchemaRegistryCompatibility(@PathParam("project") String project,
                                                                @PathParam("service_name") String service,
                                                                @PathParam("subject_name") String subject,
                                                                @PathParam("version_id") String versionId,
                                                                @NotNull SubjectSchemaRegistration schema);

    /**
     * Closes this client.
     */
    @Override
    default void close() {
    }
}
