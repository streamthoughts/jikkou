/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.api;

import io.streamthoughts.jikkou.api.model.ResourceList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceListHandlersTest {

    @Test
    void should_apply_all_handlers_in_order() {
        List<String> result = new ArrayList<>();
        ResourceListHandlers handlers = new ResourceListHandlers(List.of(
                resources -> {
                    result.add("A");
                    return resources;
                },
                resources -> {
                    result.add("B");
                    return resources;
                },
                resources -> {
                    result.add("C");
                    return resources;
                }
        ));
        handlers.handle(new ResourceList(Collections.emptyList()));
        Assertions.assertEquals("A", result.get(0));
        Assertions.assertEquals("B", result.get(1));
        Assertions.assertEquals("C", result.get(2));
    }

}