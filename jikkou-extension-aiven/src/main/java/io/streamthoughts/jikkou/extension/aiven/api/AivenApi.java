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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

/**
 * REST API for Aiven (see <a href="https://api.aiven.io/doc/">https://api.aiven.io/doc/</a>)
 */
@Path("/project/{project}/service/{service_name}/")
public interface AivenApi extends AutoCloseable {

    /**
     * Add a Kafka ACL entry.
     *
     * @param project Project name
     * @param service Service name
     * @param entry   Kafka ACL entry
     */
    @POST
    @Path("acl")
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
    @Path("acl")
    @Produces("application/json")
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
    @Path("acl/{kafka_acl_id}")
    @Produces("application/json")
    ListKafkaAclResponse deleteKafkaAclEntry(@PathParam("project") String project,
                                             @PathParam("service_name") String service,
                                             @PathParam("kafka_acl_id") String id);

    /**
     * List Schema Registry ACL entries
     *
     * @param project Project name
     * @param service Service name
     */
    @GET
    @Path("kafka/schema-registry/acl")
    @Produces("application/json")
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
    @Path("kafka/schema-registry/acl")
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
    @Path("kafka/schema-registry/acl/{schema_registry_acl_id}")
    @Produces("application/json")
    ListSchemaRegistryAclResponse deleteSchemaRegistryAclEntry(@PathParam("project") String project,
                                                               @PathParam("service_name") String service,
                                                               @PathParam("schema_registry_acl_id") String id);

    /**
     * List Kafka quotas
     *
     * @param project Project name
     * @param service Service name
     */
    @GET
    @Path("quota")
    @Produces("application/json")
    ListKafkaQuotaResponse listKafkaQuotas(@PathParam("project") String project,
                                           @PathParam("service_name") String service);

    /**
     * Create Kafka quota
     *
     * @param project Project name
     * @param service Service name
     */
    @POST
    @Path("quota")
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
    @Path("quota")
    @Consumes("application/json")
    MessageErrorsResponse deleteKafkaQuota(@PathParam("project") String project,
                                           @PathParam("service_name") String service,
                                           @QueryParam("client-id") String client,
                                           @QueryParam("user") String user);

    /**
     * Closes this client.
     */
    @Override
    default void close() {
    }
}
