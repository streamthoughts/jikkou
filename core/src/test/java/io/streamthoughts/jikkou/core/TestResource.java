/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;

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