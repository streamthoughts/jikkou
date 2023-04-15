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
package io.streamthoughts.jikkou.api.selector;

import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.model.annotations.ApiVersion;
import io.streamthoughts.jikkou.api.model.annotations.Kind;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FieldSelectorTest {

    static final TestResource TEST_RESOURCE = new TestResource()
            .withMetadata(ObjectMeta
                    .builder()
                    .withName("test-resource")
                    .withLabel("a-label.key", "value")
                    .withAnnotation("an-annotation.key", "value")
                    .build()
            );


    @Test
    public void should_select_resource_given_selector_IN() {
        // GIVEN
        FieldSelector selector = new FieldSelector();
        selector.setKey("metadata.name");
        selector.setOperator(ExpressionOperator.IN);
        selector.setValues(List.of("test-resource"));

        // WHEN
        boolean result = selector.apply(TEST_RESOURCE);

        // THEN
        Assertions.assertTrue(result);
    }

    @Test
    public void should_not_select_resource_given_selector_NOTIN() {
        // GIVEN
        FieldSelector selector = new FieldSelector();
        selector.setKey("metadata.name");
        selector.setOperator(ExpressionOperator.NOTIN);
        selector.setValues(List.of("test-resource"));

        // WHEN
        boolean result = selector.apply(TEST_RESOURCE);

        // THEN
        Assertions.assertFalse(result);
    }

    @ApiVersion("kafka.jikkou.io/v1beta2")
    @Kind("Test")
    public static class TestResource implements HasMetadata {

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

        /** {@inheritDoc} **/
        @Override
        public String getApiVersion() {
            return HasMetadata.getApiVersion(this.getClass());
        }
        /** {@inheritDoc} **/
        @Override
        public String getKind() {
            return HasMetadata.getKind(this.getClass());
        }
    }

}