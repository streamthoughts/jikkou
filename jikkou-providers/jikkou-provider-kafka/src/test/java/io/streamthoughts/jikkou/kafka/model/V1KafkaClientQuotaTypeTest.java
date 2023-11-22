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
package io.streamthoughts.jikkou.kafka.model;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaEntity;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class V1KafkaClientQuotaTypeTest {

    @Test
    void should_throw_exception_given_invalid_quota_type_user() {
        Assertions.assertThrows(JikkouRuntimeException.class, () -> {
            KafkaClientQuotaType.USER.validate(new V1KafkaClientQuotaEntity(null, null));
        });
    }

    @Test
    void should_throw_exception_given_invalid_quota_type_client() {
        Assertions.assertThrows(JikkouRuntimeException.class, () -> {
            KafkaClientQuotaType.CLIENT.validate(new V1KafkaClientQuotaEntity(null, null));
        });
    }

    @Test
    void should_throw_exception_given_invalid_quota_type_user_client() {
        Assertions.assertThrows(JikkouRuntimeException.class, () -> {
            KafkaClientQuotaType.USER_CLIENT.validate(new V1KafkaClientQuotaEntity(null, null));
        });
    }

    @Test
    void should_return_type_given_client_default() {
        final KafkaClientQuotaType result = KafkaClientQuotaType.from(new HashMap<>() {{
            put("client-id", null);
        }});
        Assertions.assertEquals(KafkaClientQuotaType.CLIENTS_DEFAULT, result);
    }

    @Test
    void should_return_type_given_client_id() {
        final KafkaClientQuotaType result = KafkaClientQuotaType.from(new HashMap<>() {{
            put("client-id", "test");
        }});
        Assertions.assertEquals(KafkaClientQuotaType.CLIENT, result);
    }

    @Test
    void should_return_type_given_user_default() {
        final KafkaClientQuotaType result = KafkaClientQuotaType.from(new HashMap<>() {{
            put("user", null);
        }});
        Assertions.assertEquals(KafkaClientQuotaType.USERS_DEFAULT, result);
    }

    @Test
    void should_return_type_given_user() {
        final KafkaClientQuotaType result = KafkaClientQuotaType.from(new HashMap<>() {{
            put("user", "test");
        }});
        Assertions.assertEquals(KafkaClientQuotaType.USER, result);
    }
}