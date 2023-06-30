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
package io.streamthoughts.jikkou.schema.registry.api;

import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityCheck;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityLevelObject;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityObject;
import io.streamthoughts.jikkou.schema.registry.api.data.SchemaString;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchema;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaId;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectVersion;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import java.util.List;

/**
 * REST API for Schema Registry implementation.
 */
@Path("/")
public interface SchemaRegistryApi {

    /*
     * ----------------------------------------------------------------------------------------------------------------
     * SUBJECTS
     * ----------------------------------------------------------------------------------------------------------------
     */

    /**
     * Gets a list of registered subjects.
     *
     * @return a list of registered subjects.
     */
    @GET
    @Path("subjects")
    @Produces("application/vnd.schemaregistry.v1+json")
    List<String> listSubjects();

    /**
     * Deletes the specified subject and its associated compatibility level if registered.
     *
     * @param subject   the name of the subject
     * @param permanent flag to specify a hard delete of the subject, which removes all associated metadata including the schema ID.
     * @return a list of versions
     */
    @DELETE
    @Path("subjects/{subject}")
    @Produces("application/vnd.schemaregistry.v1+json")
    List<Integer> deleteSubjectVersions(@PathParam("subject") String subject,
                                        @DefaultValue("false") @QueryParam("permanent") boolean permanent);

    /**
     * Gets a list of versions registered under the specified subject.
     *
     * @param subject the name of the subject
     * @return a list of versions
     */
    @GET
    @Path("subjects/{subject}/versions")
    @Produces("application/vnd.schemaregistry.v1+json")
    List<Integer> getAllSubjectVersions(@PathParam("subject") String subject);

    /**
     * Register a new schema under the specified subject.
     *
     * @param subject   the name of the subject.
     * @param schema    the schema to be registered.
     * @param normalize whether to normalize the given schema
     * @return the globally unique identifier of the schema.
     */
    @POST
    @Path("subjects/{subject}/versions")
    @Produces("application/vnd.schemaregistry.v1+json")
    @Consumes({"application/vnd.schemaregistry.v1+json", "application/vnd.schemaregistry+json", "application/json"})
    SubjectSchemaId registerSchema(@PathParam("subject") String subject,
                                   SubjectSchemaRegistration schema,
                                   @DefaultValue("false") @QueryParam("normalize") boolean normalize);

    /**
     * Check if a schema has already been registered under the specified subject.
     *
     * @param subject   the name of the subject.
     * @param schema    the schema to be checked.
     * @param normalize flag to specify to normalize the schema. The default is {@code false}.
     * @return the globally unique identifier of the schema.
     */
    @POST
    @Path("subjects/{subject}")
    @Produces("application/vnd.schemaregistry.v1+json")
    @Consumes({"application/vnd.schemaregistry.v1+json", "application/vnd.schemaregistry+json", "application/json"})
    SubjectSchema checkSubjectVersion(@PathParam("subject") String subject,
                                      SubjectSchemaRegistration schema,
                                      @DefaultValue("false") @QueryParam("normalize") boolean normalize);

    /**
     * Get the latest version of the schema registered under the specified subject.
     *
     * @param subject name of the subject
     * @return a {@link SubjectSchema} object.
     */
    @GET
    @Path("subjects/{subject}/versions/latest")
    @Produces("application/vnd.schemaregistry.v1+json")
    SubjectSchema getLatestSubjectSchema(@PathParam("subject") String subject);

    /**
     * Get a specific version of the schema registered under the specified subject.
     *
     * @param subject name of the subject
     * @param version version of the schema to be returned.
     * @return a {@link SubjectSchema} object.
     */
    @GET
    @Path("subjects/{subject}/versions/{version: [0-9]+}")
    @Produces("application/vnd.schemaregistry.v1+json")
    SubjectSchema getSchemaByVersion(@PathParam("subject") String subject, @PathParam("version") int version);

    /*
     * ----------------------------------------------------------------------------------------------------------------
     * SCHEMAS
     * ----------------------------------------------------------------------------------------------------------------
     */
    @GET
    @Path("schemas/types")
    @Produces("application/vnd.schemaregistry.v1+json")
    List<String> getSchemasTypes();

    /**
     * Get only the schema identified by the given ID.
     *
     * @param id the globally unique identifier of the schema.
     * @return the string schema.
     */
    @GET
    @Path("schemas/ids/{id: [0-9]+}")
    @Produces("application/vnd.schemaregistry.v1+json")
    SchemaString getSchemaById(@PathParam("id") String id);

    /**
     * Get only the schema identified by the given ID.
     *
     * @param id the globally unique identifier of the schema.
     * @return the string schema.
     */
    @GET
    @Path("schemas/ids/{id: [0-9]+}/schema")
    @Produces("application/vnd.schemaregistry.v1+json")
    String getSchemaOnlyById(@PathParam("id") String id);

    /**
     * Get the subject-version pairs identified by the given ID.
     *
     * @param id the globally unique identifier of the schema
     */
    @GET
    @Path("schemas/ids/{id: [0-9]+}/versions")
    @Produces("application/vnd.schemaregistry.v1+json")
    List<SubjectVersion> getVersionSchemaById(@PathParam("id") String id);

    /*
     * ----------------------------------------------------------------------------------------------------------------
     * CONFIG
     * ----------------------------------------------------------------------------------------------------------------
     */

    /**
     * Gets global compatibility level.
     *
     * @return the compatibility level.
     */
    @GET
    @Path("config")
    @Produces("application/vnd.schemaregistry.v1+json")
    CompatibilityLevelObject getGlobalCompatibility();

    /**
     * Gets compatibility level for the specified subject.
     *
     * @param subject         the name of the subject.
     * @param defaultToGlobal
     * @return the compatibility level.
     */
    @GET
    @Path("config/{subject}")
    @Produces("application/vnd.schemaregistry.v1+json")
    CompatibilityLevelObject getConfigCompatibility(@PathParam("subject") String subject,
                                                    @QueryParam("defaultToGlobal") @DefaultValue("false") boolean defaultToGlobal);

    /**
     * Updates compatibility level for the specified subject.
     *
     * @param subject       the name of the subject.
     * @param compatibility the new compatibility level for the subject.
     * @return the uptated compatibility level.
     */
    @PUT
    @Path("config/{subject}")
    @Consumes({"application/vnd.schemaregistry.v1+json", "application/vnd.schemaregistry+json", "application/json"})
    CompatibilityObject updateConfigCompatibility(@PathParam("subject") String subject, CompatibilityObject compatibility);

    /**
     * Deletes the specified subject-level compatibility level config and reverts to the global default.
     *
     * @param subject the name of the subject.
     * @return the compatibility level.
     */
    @DELETE
    @Path("config/{subject}")
    @Produces("application/vnd.schemaregistry.v1+json")
    CompatibilityObject deleteConfigCompatibility(@PathParam("subject") String subject);

    /*
     * ----------------------------------------------------------------------------------------------------------------
     * COMPATIBILITY
     * ----------------------------------------------------------------------------------------------------------------
     */
    @POST
    @Path("/compatibility/subjects/{subject}/versions/{version: [0-9]+}")
    @Produces("application/vnd.schemaregistry.v1+json")
    @Consumes({"application/vnd.schemaregistry.v1+json", "application/vnd.schemaregistry+json", "application/json"})
    CompatibilityCheck testCompatibility(@PathParam("subject") String subject,
                                         @PathParam("version") int version,
                                         @QueryParam("verbose") boolean verbose,
                                         SubjectSchemaRegistration schema);
}
