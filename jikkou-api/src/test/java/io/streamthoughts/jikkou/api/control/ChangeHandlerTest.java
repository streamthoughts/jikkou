/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.api.control;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChangeHandlerTest {

    @Test
    void shouldGetDefaultChangeHandler() {
        // Given
        ChangeHandler.None<Change> handler = new ChangeHandler.None<>(change -> () -> "NONE");
        Change change = () -> ChangeType.NONE;

        // When / Then
        Set<ChangeType> supportedChangeTypes = handler.supportedChangeTypes();
        Assertions.assertEquals(1, supportedChangeTypes.size());
        Assertions.assertEquals(ChangeType.NONE, handler.supportedChangeTypes().iterator().next());

        // When / Then
        List<ChangeResponse<Change>> applied = handler.apply(List.of(change));
        Assertions.assertEquals(1, applied.size());
        Assertions.assertEquals(change, applied.iterator().next().getChange());

        // When / Then
        ChangeDescription description = handler.getDescriptionFor(change);
        Assertions.assertNotNull(description);
        Assertions.assertEquals("NONE", description.textual());
    }
}