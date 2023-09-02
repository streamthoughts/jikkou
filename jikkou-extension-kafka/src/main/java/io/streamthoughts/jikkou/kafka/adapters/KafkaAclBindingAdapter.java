/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.adapters;

import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourcePatternFilter;

/**
 * Adapter class to map a kafka {@link AclBinding} or {@link AclBindingFilter}
 * to a {@link KafkaAclBinding} object and vice versa.
 */
public final class KafkaAclBindingAdapter {

    public static AclBinding toAclBinding(final KafkaAclBinding rule) {
        return new AclBinding(
                new ResourcePattern(rule.getResourceType(), rule.getResourcePattern(), rule.getPatternType()),
                new AccessControlEntry(rule.getPrincipal(), rule.getHost(), rule.getOperation(), rule.getType())
        );
    }

    public static KafkaAclBinding fromAclBinding(final AclBinding binding) {
        final ResourcePattern pattern = binding.pattern();
        return KafkaAclBinding.builder()
                .withResourcePattern(pattern.name())
                .withPatternType(pattern.patternType())
                .withResourceType(pattern.resourceType())
                .withOperation(binding.entry().operation())
                .withType(binding.entry().permissionType())
                .withHost(binding.entry().host())
                .withPrincipal(binding.entry().principal())
                .build();
    }

    public static AclBindingFilter toAclBindingFilter(final KafkaAclBinding rule) {
        return new AclBindingFilter(
                new ResourcePatternFilter(rule.getResourceType(), rule.getResourcePattern(), rule.getPatternType()),
                new AccessControlEntryFilter(rule.getPrincipal(), rule.getHost(), rule.getOperation(), rule.getType())
        );
    }

    public static KafkaAclBinding fromAclBindingFilter(final AclBindingFilter binding) {
        final AccessControlEntryFilter entryFilter = binding.entryFilter();
        final ResourcePatternFilter pattern = binding.patternFilter();
        return KafkaAclBinding.builder()
                .withResourcePattern(pattern.name())
                .withPatternType(pattern.patternType())
                .withResourceType(pattern.resourceType())
                .withOperation(entryFilter.operation())
                .withType(entryFilter.permissionType())
                .withHost(entryFilter.host())
                .withPrincipal(entryFilter.principal())
                .build();
    }
}
