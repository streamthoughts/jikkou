/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.kafka.change.handlers.quotas;

import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

/**
 * Operation to create client quotas.
 */
public class CreateQuotasChangeHandlerKafka extends AbstractQuotaChangeHandler {

    /**
     * Creates a new {@link CreateQuotasChangeHandlerKafka} instance.
     *
     * @param client    the {@link AdminClient}.
     */
    public CreateQuotasChangeHandlerKafka(@NotNull final AdminClient client) {
       super(client, ChangeType.ADD);
    }
}
