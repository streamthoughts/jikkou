/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zenika.kafka.specs.operation;

import com.zenika.kafka.specs.resources.ClusterResource;
import org.apache.kafka.clients.admin.AdminClient;

/**
 * Default interface to execute an administrative operation on a Kafka Cluster.
 *
 * @param <T>   type of the resource on which the command is executed.
 * @param <C>   type of the command options.
 * @param <O>   type of operation output.
 */
public interface ClusterOperation<T extends ClusterResource, C extends ResourceOperationOptions, O> {

    /**
     * Execute a specific administrative operation on the specified resource.
     *
     * @param client    the client used to run the operation.
     * @param resource  the resource to operate.
     * @param options   the operation options.
     *
     * @return
     */
    O execute(AdminClient client, T resource, C options);
}
