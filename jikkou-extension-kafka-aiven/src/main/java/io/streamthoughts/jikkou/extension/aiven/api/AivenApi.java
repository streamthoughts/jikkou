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
package io.streamthoughts.jikkou.extension.aiven.api;

import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntriesResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntriesResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

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
    KafkaAclEntriesResponse addKafkaAclEntry(@PathParam("project") String project,
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
    KafkaAclEntriesResponse listKafkaAclEntries(@PathParam("project") String project,
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
    KafkaAclEntriesResponse deleteKafkaAclEntry(@PathParam("project") String project,
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
    SchemaRegistryAclEntriesResponse listSchemaRegistryAclEntries(@PathParam("project") String project,
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
    SchemaRegistryAclEntriesResponse addSchemaRegistryAclEntry(@PathParam("project") String project,
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
    SchemaRegistryAclEntriesResponse deleteSchemaRegistryAclEntry(@PathParam("project") String project,
                                                                  @PathParam("service_name") String service,
                                                                  @PathParam("schema_registry_acl_id") String id);

    /**
     * Closes this client.
     */
    @Override
    default void close() {
    }
}
