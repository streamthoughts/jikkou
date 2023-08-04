/*
 * Copyright 2020 The original authors
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
package io.streamthoughts.jikkou.api.io;

import io.streamthoughts.jikkou.api.model.Resource;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.io.OutputStream;
import java.util.List;

/**
 * Default interface to write a cluster specification.
 */
@InterfaceStability.Evolving
public interface ResourceWriter {

    /**
     * Writes the cluster specification into the specified output stream.
     *
     * @param items  the list of {@link Resource} to write.
     * @param os        the output stream.
     */
    void write(final List<? extends Object> items, final OutputStream os);
}
