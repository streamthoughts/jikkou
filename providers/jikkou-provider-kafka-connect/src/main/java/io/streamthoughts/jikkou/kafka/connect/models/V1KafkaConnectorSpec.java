/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.Map;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@JsonPropertyOrder({
    "connectorClass",
    "tasksMax",
    "config",
    "state"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaConnectorSpec {

    /**
     * Name or alias of the class for this connector. Must be a subclass of org.apache.kafka.connect.connector.Connector.
     * 
     */
    @JsonProperty("connectorClass")
    @JsonPropertyDescription("Name or alias of the class for this connector. Must be a subclass of org.apache.kafka.connect.connector.Connector.")
    private java.lang.String connectorClass;
    /**
     * The maximum number of tasks for the Kafka Connector.
     * 
     */
    @JsonProperty("tasksMax")
    @JsonPropertyDescription("The maximum number of tasks for the Kafka Connector.")
    private Integer tasksMax;
    /**
     * Configuration properties of the connector.
     * 
     */
    @JsonProperty("config")
    @JsonPropertyDescription("Configuration properties of the connector.")
    private Map<String, Object> config;
    /**
     * The state the connector should be in [running, stopped, paused]. Defaults to running.
     * 
     */
    @JsonProperty("state")
    @JsonPropertyDescription("The state the connector should be in [running, stopped, paused]. Defaults to running.")
    @Builder.Default
    private KafkaConnectorState state = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaConnectorSpec() {
    }

    /**
     * 
     * @param state
     * @param connectorClass
     * @param tasksMax
     * @param config
     */
    @ConstructorProperties({
        "connectorClass",
        "tasksMax",
        "config",
        "state"
    })
    public V1KafkaConnectorSpec(java.lang.String connectorClass, Integer tasksMax, Map<String, Object> config, KafkaConnectorState state) {
        super();
        this.connectorClass = connectorClass;
        this.tasksMax = tasksMax;
        this.config = config;
        this.state = state;
    }

    /**
     * Name or alias of the class for this connector. Must be a subclass of org.apache.kafka.connect.connector.Connector.
     * 
     */
    @JsonProperty("connectorClass")
    public java.lang.String getConnectorClass() {
        return connectorClass;
    }

    /**
     * The maximum number of tasks for the Kafka Connector.
     * 
     */
    @JsonProperty("tasksMax")
    public Integer getTasksMax() {
        return tasksMax;
    }

    /**
     * Configuration properties of the connector.
     * 
     */
    @JsonProperty("config")
    public Map<String, Object> getConfig() {
        return config;
    }

    /**
     * The state the connector should be in [running, stopped, paused]. Defaults to running.
     * 
     */
    @JsonProperty("state")
    public KafkaConnectorState getState() {
        return state;
    }

    @Override
    public java.lang.String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaConnectorSpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("connectorClass");
        sb.append('=');
        sb.append(((this.connectorClass == null)?"<null>":this.connectorClass));
        sb.append(',');
        sb.append("tasksMax");
        sb.append('=');
        sb.append(((this.tasksMax == null)?"<null>":this.tasksMax));
        sb.append(',');
        sb.append("config");
        sb.append('=');
        sb.append(((this.config == null)?"<null>":this.config));
        sb.append(',');
        sb.append("state");
        sb.append('=');
        sb.append(((this.state == null)?"<null>":this.state));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.state == null)? 0 :this.state.hashCode()));
        result = ((result* 31)+((this.connectorClass == null)? 0 :this.connectorClass.hashCode()));
        result = ((result* 31)+((this.tasksMax == null)? 0 :this.tasksMax.hashCode()));
        result = ((result* 31)+((this.config == null)? 0 :this.config.hashCode()));
        return result;
    }

    @Override
    public boolean equals(java.lang.Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaConnectorSpec) == false) {
            return false;
        }
        V1KafkaConnectorSpec rhs = ((V1KafkaConnectorSpec) other);
        return (((((this.state == rhs.state)||((this.state!= null)&&this.state.equals(rhs.state)))&&((this.connectorClass == rhs.connectorClass)||((this.connectorClass!= null)&&this.connectorClass.equals(rhs.connectorClass))))&&((this.tasksMax == rhs.tasksMax)||((this.tasksMax!= null)&&this.tasksMax.equals(rhs.tasksMax))))&&((this.config == rhs.config)||((this.config!= null)&&this.config.equals(rhs.config))));
    }

}
