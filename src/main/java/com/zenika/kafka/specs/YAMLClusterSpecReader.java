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

import com.zenika.kafka.specs.acl.AclGroupPolicy;
import com.zenika.kafka.specs.acl.AclUserPolicy;
import com.zenika.kafka.specs.reader.AclGroupPolicyReader;
import com.zenika.kafka.specs.reader.AclUserPolicyReader;
import com.zenika.kafka.specs.reader.EntitySpecificationReader;
import com.zenika.kafka.specs.reader.MapObjectReader;
import com.zenika.kafka.specs.reader.TopicClusterSpecReader;
import com.zenika.kafka.specs.resources.TopicResource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.zenika.kafka.specs.ClusterSpecReader.Fields.ACL_FIELD;
import static com.zenika.kafka.specs.ClusterSpecReader.Fields.ACL_GROUP_POLICIES_FIELD;
import static com.zenika.kafka.specs.ClusterSpecReader.Fields.ACL_ACCESS_POLICIES_FIELD;
import static com.zenika.kafka.specs.ClusterSpecReader.Fields.TOPICS_FIELD;

/**
 * Class used to read a Kafka cluster specification a from a YAML input file.
 */
public class YAMLClusterSpecReader implements ClusterSpecReader {

    private final TopicClusterSpecReader TOPIC_READER = new TopicClusterSpecReader();
    private final AclGroupPolicyReader ACL_GROUP_READER = new AclGroupPolicyReader();
    private final AclUserPolicyReader ACL_USER_READER = new AclUserPolicyReader();

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public ClusterSpec read(final InputStream stream) {
        Yaml yaml = new Yaml();
        Map<String, Object> specification = yaml.load(stream);
        requireNonNull(specification, "Cluster specification is empty or invalid.");

        final Set<TopicResource> mTopics = read(TOPICS_FIELD, specification, TOPIC_READER);

        Map<String, Object> acls = (Map<String, Object>) specification.get(ACL_FIELD);

        final Set<AclGroupPolicy> mGroups = read(ACL_GROUP_POLICIES_FIELD, acls, ACL_GROUP_READER);
        final Set<AclUserPolicy> sUsers = read(ACL_ACCESS_POLICIES_FIELD, acls, ACL_USER_READER);

        return new ClusterSpec(mTopics, mGroups, sUsers);
    }

    /**
     * Read the specification entity with the specified reader.
     *
     * @param key       the entity key to read.
     * @param input     the specification input.
     * @param reader    the reader
     * @param <T>       the output entity type.
     *
     * @return          a set of new {@link T} instance.
     */
    private static <T> Set<T> read(final String key, final Map<String, Object> input, final EntitySpecificationReader<T> reader) {
        if (input == null) return Collections.emptySet();

        return Optional.ofNullable(input.get(key))
            .map(t -> reader.read(MapObjectReader.toList(t)))
            .orElse(Collections.emptySet());
    }

    private static void requireNonNull(final Object o, final String message) {
        if (o == null) {
            throw new InvalidSpecificationException(message);
        }
    }
}
