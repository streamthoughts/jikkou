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
package com.zenika.kafka.specs.reader;

import com.zenika.kafka.specs.acl.AclOperationPolicy;
import com.zenika.kafka.specs.acl.AclRulesBuilder;
import com.zenika.kafka.specs.acl.AclUserPolicy;
import com.zenika.kafka.specs.acl.AclUserPolicyBuilder;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A reader for {@link AclUserPolicy}.
 */
public class AclUserPolicyReader implements EntitySpecificationReader<AclUserPolicy> {

    public static final String ACL_RESOURCE_FIELD              = "resource";
    public static final String ACL_PRINCIPAL_FIELD             = "principal";
    public static final String ACL_GROUPS_FIELD                = "groups";
    public static final String ACL_TYPE_FIELD                  = "type";
    public static final String ACL_PATTERN_FIELD               = "pattern";
    public static final String ACL_PATTERN_TYPE_FIELD          = "patternType";
    public static final String ACL_PERMISSION_FIELD            = "permissions";
    public static final String ACL_ALLOW_OPERATIONS_FIELD      = "allow_operations";

    /**
     * @return {@link AclUserPolicy} instance.
     */
    @Override
    public AclUserPolicy to(final MapObjectReader entry) {

        AclUserPolicyBuilder builder =
                AclUserPolicyBuilder.newBuilder()
                        .principal(entry.get(ACL_PRINCIPAL_FIELD))
                        .groups(entry.get(ACL_GROUPS_FIELD));

        List<MapObjectReader> permissions = entry.getMapList(ACL_PERMISSION_FIELD);

        permissions.forEach(p -> {
            MapObjectReader resource = p.getMapObject(ACL_RESOURCE_FIELD);

            final String pattern = resource.get(ACL_PATTERN_FIELD);

            final ResourceType type = ResourceType.fromString(resource.get(ACL_TYPE_FIELD));
            final PatternType patternType = PatternType.fromString(resource.get(ACL_PATTERN_TYPE_FIELD));

            List<String> operations = p.get(ACL_ALLOW_OPERATIONS_FIELD);

            Set<AclOperationPolicy> policies = operations.stream()
                    .map(AclOperationPolicy::fromString)
                    .collect(Collectors.toSet());

            operations.forEach(o -> builder.addPermission(pattern, patternType, type, policies));
        });

        return builder.build();
    }
}