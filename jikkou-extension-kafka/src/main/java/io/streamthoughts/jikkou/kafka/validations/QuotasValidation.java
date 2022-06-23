/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.validations;

import io.streamthoughts.jikkou.api.AcceptResource;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceValidation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaQuotaList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaQuotaObject;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Validation for {@link V1KafkaQuotaList}.
 */
@AcceptResource(type = V1KafkaQuotaList.class)
public abstract class QuotasValidation implements ResourceValidation {

    /**
     * {@inheritDoc}
     */
    public void validate(@NotNull final HasMetadata resource) throws ValidationException {
        V1KafkaQuotaList quotaResource = (V1KafkaQuotaList) resource;

        List<V1KafkaQuotaObject> quotas = quotaResource.getSpec().getQuotas();
        if (quotas == null || quotas.isEmpty()) return;

        List<ValidationException> exceptions = new ArrayList<>(quotas.size());
        for (V1KafkaQuotaObject quota : quotas) {
            try {
                validateQuota(quota);
            } catch (ValidationException e) {
                exceptions.add(e);
            }
        }
        if (!exceptions.isEmpty()) {
            throw new ValidationException(exceptions);
        }
    }

    public abstract void validateQuota(@NotNull final V1KafkaQuotaObject quota) throws ValidationException;
}
