/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
                new ResourcePattern(rule.resourceType(), rule.resourcePattern(), rule.patternType()),
                new AccessControlEntry(rule.principal(), rule.host(), rule.operation(), rule.type())
        );
    }

    public static KafkaAclBinding fromAclBinding(final AclBinding binding) {
        final ResourcePattern pattern = binding.pattern();
        return new KafkaAclBinding(
                binding.entry().principal(),
                pattern.name(),
                pattern.patternType(),
                pattern.resourceType(),
                binding.entry().operation(),
                binding.entry().permissionType(),
                binding.entry().host()
        );
    }

    public static AclBindingFilter toAclBindingFilter(final KafkaAclBinding rule) {
        return new AclBindingFilter(
                new ResourcePatternFilter(rule.resourceType(), rule.resourcePattern(), rule.patternType()),
                new AccessControlEntryFilter(rule.principal(), rule.host(), rule.operation(), rule.type())
        );
    }

    public static KafkaAclBinding fromAclBindingFilter(final AclBindingFilter binding) {
        final AccessControlEntryFilter entryFilter = binding.entryFilter();
        final ResourcePatternFilter pattern = binding.patternFilter();
        return new KafkaAclBinding(
                entryFilter.principal(),
                pattern.name(),
                pattern.patternType(),
                pattern.resourceType(),
                entryFilter.operation(),
                entryFilter.permissionType(),
                entryFilter.host()
        );
    }
}
