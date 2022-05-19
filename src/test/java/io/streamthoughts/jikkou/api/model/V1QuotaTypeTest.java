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
package io.streamthoughts.jikkou.api.model;

import io.streamthoughts.jikkou.api.error.JikkouException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class V1QuotaTypeTest {

    @Test
    public void should_throw_exception_given_invalid_quota_type_user() {
        Assertions.assertThrows(JikkouException.class, () -> {
            V1QuotaType.USER.validate(new V1QuotaEntityObject(null, null));
        });
    }

    @Test
    public void should_throw_exception_given_invalid_quota_type_client() {
        Assertions.assertThrows(JikkouException.class, () -> {
            V1QuotaType.CLIENT.validate(new V1QuotaEntityObject(null, null));
        });
    }

    @Test
    public void should_throw_exception_given_invalid_quota_type_user_client() {
        Assertions.assertThrows(JikkouException.class, () -> {
            V1QuotaType.USER_CLIENT.validate(new V1QuotaEntityObject(null, null));
        });
    }

    @Test
    public void should_return_type_given_client_default() {
        final V1QuotaType result = V1QuotaType.from(new HashMap<>() {{
            put("client-id", null);
        }});
        Assertions.assertEquals(V1QuotaType.CLIENTS_DEFAULT, result);
    }

    @Test
    public void should_return_type_given_client_id() {
        final V1QuotaType result = V1QuotaType.from(new HashMap<>() {{
            put("client-id", "test");
        }});
        Assertions.assertEquals(V1QuotaType.CLIENT, result);
    }

    @Test
    public void should_return_type_given_user_default() {
        final V1QuotaType result = V1QuotaType.from(new HashMap<>() {{
            put("user", null);
        }});
        Assertions.assertEquals(V1QuotaType.USERS_DEFAULT, result);
    }

    @Test
    public void should_return_type_given_user() {
        final V1QuotaType result = V1QuotaType.from(new HashMap<>() {{
            put("user", "test");
        }});
        Assertions.assertEquals(V1QuotaType.USER, result);
    }
}