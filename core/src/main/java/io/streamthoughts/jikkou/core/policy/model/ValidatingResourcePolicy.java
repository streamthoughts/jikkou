/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.policy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Resource;
import io.streamthoughts.jikkou.core.annotation.Transient;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.SpecificResource;
import java.beans.ConstructorProperties;

@Resource
@ApiVersion("core.jikkou.io/v1")
@Kind("ValidatingResourcePolicy")
@Transient
@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ValidatingResourcePolicy extends SpecificResource<ValidatingResourcePolicy, ValidatingResourcePolicySpec> {

    /**
     * Creates a new {@link ValidatingResourcePolicy} instance.
     *
     * @param apiVersion The resource API Version.
     * @param kind       The resource Kind.
     * @param metadata   The resource metadata.
     * @param spec       The resource spec.
     */
    @ConstructorProperties({
        "apiVersion",
        "kind",
        "metadata",
        "spec"
    })
    public ValidatingResourcePolicy(String apiVersion,
                                    String kind,
                                    ObjectMeta metadata,
                                    ValidatingResourcePolicySpec spec) {
        super(apiVersion, kind, metadata, spec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Builder toBuilder() {
        return new Builder()
            .withApiVersion(apiVersion)
            .withKind(kind)
            .withMetadata(metadata)
            .withSpec(spec);
    }

    /**
     * Builder for constructing new {@link ValidatingResourcePolicy} objects.
     */
    public static class Builder extends SpecificResource.Builder<Builder, ValidatingResourcePolicy, ValidatingResourcePolicySpec> {

        /**
         * {@inheritDoc}
         */
        @Override
        public ValidatingResourcePolicy build() {
            return new ValidatingResourcePolicy(
                apiVersion,
                kind,
                metadata,
                spec
            );
        }
    }
}
