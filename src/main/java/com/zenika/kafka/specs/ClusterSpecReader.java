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
package com.zenika.kafka.specs;

import java.io.InputStream;

/**
 * Default interface to read a cluster specification.
 */
public interface ClusterSpecReader {

    class Fields {
        static final String TOPIC_PARTITIONS_FIELD          = "partitions";
        static final String TOPIC_REPLICATION_FACTOR_FIELD  = "replication_factor";
        static final String TOPIC_NAME_FIELD                = "name";
        static final String TOPIC_CONFIGS_FIELD             = "configs";
        static final String TOPICS_FIELD                    = "topics";
    }

    /**
     * Retrieves a {@link ClusterSpec} from the specified input stream.
     *
     * @param stream    the input stream from which to read the specification.
     * @return          a new {@link ClusterSpec} instance.
     */
    ClusterSpec read(final InputStream stream);
}
