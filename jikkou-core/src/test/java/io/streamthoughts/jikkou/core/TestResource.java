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
    public TestResource withMetadata(ObjectMeta objectMeta) {
        this.meta = objectMeta;
        return this;
    }
}