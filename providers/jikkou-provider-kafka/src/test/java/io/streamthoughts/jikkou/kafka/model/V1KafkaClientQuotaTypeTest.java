/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.model;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class V1KafkaClientQuotaTypeTest {

    @Test
    void should_throw_exception_given_invalid_quota_type_user() {
        Assertions.assertThrows(JikkouRuntimeException.class, () -> {
            KafkaClientQuotaType.USER.validate(new KafkaClientQuotaEntity(null, null));
        });
    }

    @Test
    void should_throw_exception_given_invalid_quota_type_client() {
        Assertions.assertThrows(JikkouRuntimeException.class, () -> {
            KafkaClientQuotaType.CLIENT.validate(new KafkaClientQuotaEntity(null, null));
        });
    }

    @Test
    void should_throw_exception_given_invalid_quota_type_user_client() {
        Assertions.assertThrows(JikkouRuntimeException.class, () -> {
            KafkaClientQuotaType.USER_CLIENT.validate(new KafkaClientQuotaEntity(null, null));
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