/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.api;

import io.jikkou.extension.confluent.api.data.RoleBindingData;
import io.jikkou.extension.confluent.api.data.RoleBindingListResponse;
import io.jikkou.extension.confluent.api.data.ServiceAccountListResponse;
import io.jikkou.extension.confluent.api.data.UserListResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

/**
 * REST API for Confluent Cloud IAM v2.
 *
 * @see <a href="https://docs.confluent.io/cloud/current/api.html#tag/Role-Bindings-(iam/v2)">API docs</a>
 */
@Path("/iam/v2")
@Produces("application/json")
public interface ConfluentCloudApi extends AutoCloseable {

    /**
     * List role bindings.
     *
     * @param crnPattern CRN pattern to filter role bindings.
     * @param pageSize   Maximum number of results per page (max 100).
     * @param pageToken  Token for the next page.
     * @return the list response with pagination metadata.
     */
    @GET
    @Path("/role-bindings")
    RoleBindingListResponse listRoleBindings(
        @QueryParam("crn_pattern") String crnPattern,
        @QueryParam("page_size") Integer pageSize,
        @QueryParam("page_token") String pageToken);

    /**
     * Create a role binding.
     *
     * @param data the role binding to create.
     * @return the created role binding.
     */
    @POST
    @Path("/role-bindings")
    @Consumes("application/json")
    RoleBindingData createRoleBinding(RoleBindingData data);

    /**
     * Get a role binding by ID.
     *
     * @param id the role binding ID.
     * @return the role binding.
     */
    @GET
    @Path("/role-bindings/{id}")
    RoleBindingData getRoleBinding(@PathParam("id") String id);

    /**
     * Delete a role binding by ID.
     *
     * @param id the role binding ID.
     */
    @DELETE
    @Path("/role-bindings/{id}")
    void deleteRoleBinding(@PathParam("id") String id);

    /*
     * ----------------------------------------------------------------------------------------------------------------
     * USERS
     * ----------------------------------------------------------------------------------------------------------------
     */

    /**
     * List users.
     *
     * @param pageSize  Maximum number of results per page (max 100).
     * @param pageToken Token for the next page.
     * @return the list response with pagination metadata.
     */
    @GET
    @Path("/users")
    UserListResponse listUsers(
        @QueryParam("page_size") Integer pageSize,
        @QueryParam("page_token") String pageToken);

    /*
     * ----------------------------------------------------------------------------------------------------------------
     * SERVICE ACCOUNTS
     * ----------------------------------------------------------------------------------------------------------------
     */

    /**
     * List service accounts.
     *
     * @param pageSize  Maximum number of results per page (max 100).
     * @param pageToken Token for the next page.
     * @return the list response with pagination metadata.
     */
    @GET
    @Path("/service-accounts")
    ServiceAccountListResponse listServiceAccounts(
        @QueryParam("page_size") Integer pageSize,
        @QueryParam("page_token") String pageToken);

    /**
     * {@inheritDoc}
     */
    @Override
    default void close() {
    }
}
