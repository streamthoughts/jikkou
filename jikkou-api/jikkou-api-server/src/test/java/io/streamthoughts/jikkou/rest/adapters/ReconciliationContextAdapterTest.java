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
package io.streamthoughts.jikkou.rest.adapters;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.selectors.ExpressionSelectorFactory;
import io.streamthoughts.jikkou.rest.data.ResourceListRequest;
import io.streamthoughts.jikkou.rest.data.ResourceReconcileRequest;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReconciliationContextAdapterTest {

    @Test
    void shouldGetCtxForResourceReconcileRequest() {
        ReconciliationContextAdapter adapter = new ReconciliationContextAdapter(new ExpressionSelectorFactory());
        ReconciliationContext context = adapter.getReconciliationContext(new ResourceReconcileRequest(
                new ResourceReconcileRequest.Params(),
                Collections.emptyList()
        ), false);
        Assertions.assertNotNull(context);
    }

    @Test
    void shouldGetCtxForResourceListRequest() {
        ReconciliationContextAdapter adapter = new ReconciliationContextAdapter(new ExpressionSelectorFactory());
        ReconciliationContext context = adapter.getReconciliationContext(new ResourceListRequest());
        Assertions.assertNotNull(context);

    }
}