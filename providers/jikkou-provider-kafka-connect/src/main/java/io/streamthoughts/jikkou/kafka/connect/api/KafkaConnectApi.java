/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.api;

import io.streamthoughts.jikkou.kafka.connect.api.data.ConnectCluster;
import io.streamthoughts.jikkou.kafka.connect.api.data.ConnectorCreateRequest;
import io.streamthoughts.jikkou.kafka.connect.api.data.ConnectorInfoResponse;
import io.streamthoughts.jikkou.kafka.connect.api.data.ConnectorStatusResponse;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * REST API for Kafka Connect implementation.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public interface KafkaConnectApi extends AutoCloseable {

    /*
     * ----------------------------------------------------------------------------------------------------------------
     * CONNECT CLUSTER
     * ----------------------------------------------------------------------------------------------------------------
     */

    /**
     * Gets root.
     */
    @GET
    ConnectCluster getConnectCluster();

    /*
     * ----------------------------------------------------------------------------------------------------------------
     * CONNECTORS
     * ----------------------------------------------------------------------------------------------------------------
     */

    /**
     * Gets a list of connectors.
     *
     * @return a list of connectors
     */
    @GET
    @Path("connectors")
    List<String> listConnectors();

    /**
     * Create a new connector with the given configuration and optional initial state.
     * This method supports KIP-980, allowing the connector to be created in a specific
     * initial state (RUNNING, STOPPED, or PAUSED).
     *
     * @param request the connector creation request containing name, config, and optional initial state.
     * @return information about the created connector
     */
    @POST
    @Path("connectors")
    @Consumes(MediaType.APPLICATION_JSON)
    ConnectorInfoResponse createConnector(ConnectorCreateRequest request);

    /**
     * Create a new connector using the given configuration, or update the configuration
     * for an existing connector. Returns information about the connector after the change has been made
     *
     * @param name   the connector's name.
     * @param config the connector's configuration.
     * @return a list of connectors
     */
    @PUT
    @Path("connectors/{name}/config")
    @Consumes(MediaType.APPLICATION_JSON)
    ConnectorInfoResponse createOrUpdateConnector(@PathParam("name") String name,
                                                  Map<String, Object> config);

    /**
     * Get information about the connector.
     *
     * @param name the connector's name.
     * @return a list of connectors
     */
    @GET
    @Path("connectors/{name}")
    ConnectorInfoResponse getConnector(@PathParam("name") String name);

    /**
     * Get information about the connector.
     *
     * @param name the connector's name.
     * @return a connector's config.
     */
    @GET
    @Path("connectors/{name}/config")
    Map<String, Object> getConnectorConfig(@PathParam("name") String name);

    /**
     * Get information about the connector.
     *
     * @param name the connector's name.
     * @return a connector's config.
     */
    @GET
    @Path("connectors/{name}/status")
    ConnectorStatusResponse getConnectorStatus(@PathParam("name") String name);


    /**
     * Delete a connector, halting all tasks and deleting its configuration.
     *
     * @param name the connector's name.
     */
    @DELETE
    @Path("connectors/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    void deleteConnector(@PathParam("name") String name);

    /**
     * Completely shut down the connector and its tasks. The connector config remains present in the config topic of
     * the cluster (if running in distributed mode), unmodified.
     *
     * @param name the connector's name.
     */
    @PUT
    @Path("connectors/{name}/stop")
    @Produces(MediaType.APPLICATION_JSON)
    void stopConnector(@PathParam("name") String name);

    /**
     * Pause the connector and its tasks, which stops message processing until the connector is resumed.
     * This call asynchronous and the tasks will not transition to PAUSED state at the same time.
     *
     * @param name the connector's name.
     */
    @PUT
    @Path("connectors/{name}/pause")
    @Produces(MediaType.APPLICATION_JSON)
    void pauseConnector(@PathParam("name") String name);

    /**
     * Resume a paused connector or do nothing if the connector is not paused.
     * This call asynchronous and the tasks will not transition to RUNNING state at the same time.
     *
     * @param name the connector's name.
     */
    @PUT
    @Path("connectors/{name}/resume")
    @Produces(MediaType.APPLICATION_JSON)
    void resumeConnector(@PathParam("name") String name);

    /**
     * Resume a paused connector or do nothing if the connector is not paused.
     * This call asynchronous and the tasks will not transition to RUNNING state at the same time.
     *
     * @param name         the connector's name.
     * @param includeTasks Specifies whether to restart the connector instance and task instances (includeTasks=true`)
     *                     or just the connector instance (includeTasks=false)
     * @param onlyFailed   Specifies whether to restart just the instances with a FAILED status (onlyFailed=true)
     *                     or all instances (onlyFailed=false)
     */
    @POST
    @Path("connectors/{name}/restart")
    Response restartConnector(@PathParam("name") String name,
                              @DefaultValue("false") @QueryParam("includeTasks") boolean includeTasks,
                              @DefaultValue("false") @QueryParam("onlyFailed") boolean onlyFailed);


    /**
     * Closes this client.
     */
    @Override
    default void close() {
    }
}
