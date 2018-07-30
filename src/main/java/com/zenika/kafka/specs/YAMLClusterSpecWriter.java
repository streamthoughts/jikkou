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

import com.zenika.kafka.specs.acl.AclUserPolicy;
import com.zenika.kafka.specs.resources.Configs;
import com.zenika.kafka.specs.resources.TopicResource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.zenika.kafka.specs.ClusterSpecReader.Fields.ACL_ACCESS_POLICIES_FIELD;
import static com.zenika.kafka.specs.reader.AclUserPolicyReader.*;
import static com.zenika.kafka.specs.reader.TopicClusterSpecReader.TOPIC_CONFIGS_FIELD;
import static com.zenika.kafka.specs.reader.TopicClusterSpecReader.TOPIC_NAME_FIELD;
import static com.zenika.kafka.specs.reader.TopicClusterSpecReader.TOPIC_PARTITIONS_FIELD;
import static com.zenika.kafka.specs.reader.TopicClusterSpecReader.TOPIC_REPLICATION_FACTOR_FIELD;

/**
 * Default interface to write a cluster specification.
 */
public class YAMLClusterSpecWriter implements ClusterSpecWriter {

    private static YAMLClusterSpecWriter INSTANCE = new YAMLClusterSpecWriter();

    public static YAMLClusterSpecWriter instance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final ClusterSpec spec, final OutputStream os) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        yaml.setBeanAccess(BeanAccess.DEFAULT);
        yaml.dump(toWritableMap(spec), new OutputStreamWriter(os) );
    }

    private Map<String, Object> toWritableMap(final ClusterSpec spec) {
        Map<String, Object> outputClusterMap = new HashMap<>();

        outputClusterMap.put(ClusterSpecReader.Fields.ACL_FIELD, new HashMap<String, Object>(){{
            put(ACL_ACCESS_POLICIES_FIELD, usersToMap(spec));
        }});
        outputClusterMap.put(ClusterSpecReader.Fields.TOPICS_FIELD, topicsToMap(spec) );

        return outputClusterMap;
    }

    private List<Map<String, Object>> usersToMap(ClusterSpec spec) {
        List<Map<String, Object>> outputUsers = new LinkedList<>();
        for (AclUserPolicy policy : spec.getAclUsers()) {
            Map<String, Object> user = new LinkedHashMap<>();
            user.put(ACL_PRINCIPAL_FIELD, policy.principal());
            user.put(ACL_GROUPS_FIELD, policy.groups().toArray());

            List<Map<String, Object>> permissions = new LinkedList<>();
            policy.permissions().forEach(c -> {
                Map<String, Object> p = new HashMap<>();
                p.put(ACL_RESOURCE_FIELD,  new HashMap<String, Object>(){{
                    put(ACL_RESOURCE_FIELD, c.pattern());
                    put(ACL_TYPE_FIELD, c.getType().name());

                }});
                p.put(ACL_ALLOW_OPERATIONS_FIELD, c.operationLiterals());
                permissions.add(p);
            });
            user.put(ACL_PERMISSION_FIELD, permissions);
            outputUsers.add(user);
        }
        return outputUsers;
    }

    private List<Map<String, Object>> topicsToMap(ClusterSpec spec) {
        List<Map<String, Object>> outputTopics = new LinkedList<>();
        for (TopicResource resource : spec.getTopics()) {
            Map<String, Object> outputTopicMap = new LinkedHashMap<>();
            outputTopicMap.put(TOPIC_NAME_FIELD, resource.name());
            outputTopicMap.put(TOPIC_PARTITIONS_FIELD, resource.partitions());
            outputTopicMap.put(TOPIC_REPLICATION_FACTOR_FIELD, resource.replicationFactor());
            outputTopicMap.put(TOPIC_CONFIGS_FIELD, Configs.asStringValueMap(resource.configs()));
            outputTopics.add(outputTopicMap);
        }
        return outputTopics;
    }
}
