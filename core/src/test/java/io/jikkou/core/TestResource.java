/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core;

import io.jikkou.core.annotation.ApiVersion;
import io.jikkou.core.annotation.Kind;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.ObjectMeta;

@ApiVersion("core/v1")
@Kind("Test")
public class TestResource implements HasMetadata {

    private ObjectMeta meta;
    /** {@inheritDoc} **/
    @Override
    public ObjectMeta getMetadata() {
        return meta;
    }
    /** {@inheritDoc} **/
    @Override
    public TestResource withMetadata(ObjectMeta metadata) {
        this.meta = metadata;
        return this;
    }
}