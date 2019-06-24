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
package io.streamthoughts.kafka.specs.reader;

import io.streamthoughts.kafka.specs.acl.AclGroupPolicy;
import io.streamthoughts.kafka.specs.acl.AclGroupPolicyBuilder;
import io.streamthoughts.kafka.specs.acl.AclOperationPolicy;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.List;

/**
 * A reader for {@link AclGroupPolicy}.
 */
public class AclGroupPolicyReader implements EntitySpecificationReader<AclGroupPolicy> {

    public static final String ACL_NAME__FIELD                 = "name";
    public static final String ACL_RESOURCE_FIELD              = "resource";
    public static final String ACL_TYPE_FIELD                  = "type";
    public static final String ACL_PATTERN_FIELD               = "pattern";
    public static final String ACL_PATTERN_TYPE_FIELD          = "patternType";
    public static final String ACL_ALLOW_OPERATIONS_FIELD      = "allow_operations";

    /**
     * @return a new {@link AclGroupPolicy}.
     */
    @Override
    public AclGroupPolicy to(final MapObjectReader group) {

        MapObjectReader resource = group.getMapObject(ACL_RESOURCE_FIELD);

        ResourceType resourceType = ResourceType.fromString(resource.get(ACL_TYPE_FIELD));
        String pattern = resource.get(ACL_PATTERN_FIELD);
        PatternType patternType = PatternType.fromString(resource.get(ACL_PATTERN_TYPE_FIELD));
        String name = group.get(ACL_NAME__FIELD);

        AclGroupPolicyBuilder builder =
                AclGroupPolicyBuilder.newBuilder()
                        .withName(name)
                        .withPattern(pattern)
                        .withPatternType(patternType)
                        .onResourceType(resourceType);

        List<String> operations = group.get(ACL_ALLOW_OPERATIONS_FIELD);
        operations.forEach(o -> builder.allow(AclOperationPolicy.fromString(o)));

        return builder.build();
    }
}