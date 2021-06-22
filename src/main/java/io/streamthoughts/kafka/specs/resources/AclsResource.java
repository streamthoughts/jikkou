package io.streamthoughts.kafka.specs.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.kafka.specs.acl.AclGroupPolicy;
import io.streamthoughts.kafka.specs.acl.AclUserPolicy;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class AclsResource {

    final Collection<AclGroupPolicy> aclGroupPolicies;

    final Collection<AclUserPolicy> aclUsers;

    @JsonCreator
    public AclsResource(@JsonProperty("group_policies") final Collection<AclGroupPolicy> aclGroupPolicies,
                        @JsonProperty("access_policies") final Collection<AclUserPolicy> aclUsers) {
        this.aclGroupPolicies = Optional.ofNullable(aclGroupPolicies).orElse(Collections.emptyList());
        this.aclUsers = Optional.ofNullable(aclUsers).orElse(Collections.emptyList());
    }

    @JsonProperty("group_policies")
    public Collection<AclGroupPolicy> getAclGroupPolicies() {
        return aclGroupPolicies;
    }

    @JsonProperty("access_policies")
    public Collection<AclUserPolicy> getAclUsersPolicies() {
        return aclUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AclsResource)) return false;
        AclsResource that = (AclsResource) o;
        return Objects.equals(aclGroupPolicies, that.aclGroupPolicies) &&
                Objects.equals(aclUsers, that.aclUsers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(aclGroupPolicies, aclUsers);
    }
}
