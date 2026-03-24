/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.confluent.api;

import io.streamthoughts.jikkou.extension.confluent.api.data.RoleBindingData;
import io.streamthoughts.jikkou.extension.confluent.api.data.RoleBindingListResponse;
import io.streamthoughts.jikkou.extension.confluent.api.data.ServiceAccountData;
import io.streamthoughts.jikkou.extension.confluent.api.data.ServiceAccountListResponse;
import io.streamthoughts.jikkou.extension.confluent.api.data.UserData;
import io.streamthoughts.jikkou.extension.confluent.api.data.UserListResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Confluent Cloud API client wrapper that handles pagination and pre-fills CRN pattern.
 */
public final class ConfluentCloudApiClient implements AutoCloseable {

    private static final int DEFAULT_PAGE_SIZE = 100;

    private final ConfluentCloudApi api;
    private final String crnPattern;

    /**
     * Creates a new {@link ConfluentCloudApiClient} instance.
     *
     * @param api        the REST API proxy.
     * @param crnPattern the CRN pattern for scoping list operations.
     */
    public ConfluentCloudApiClient(@NotNull final ConfluentCloudApi api,
                                   @NotNull final String crnPattern) {
        this.api = Objects.requireNonNull(api, "api must not be null");
        this.crnPattern = Objects.requireNonNull(crnPattern, "crnPattern must not be null");
    }

    /**
     * Lists all role bindings matching the configured CRN pattern, handling pagination.
     *
     * @return all role bindings.
     */
    public List<RoleBindingData> listRoleBindings() {
        List<RoleBindingData> allBindings = new ArrayList<>();
        String pageToken = null;
        do {
            RoleBindingListResponse response = api.listRoleBindings(crnPattern, DEFAULT_PAGE_SIZE, pageToken);
            if (response.data() != null) {
                allBindings.addAll(response.data());
            }
            pageToken = response.metadata() != null ? response.metadata().pageToken() : null;
        } while (pageToken != null);
        return allBindings;
    }

    /**
     * Creates a role binding.
     *
     * @param data the role binding to create.
     * @return the created role binding.
     */
    public RoleBindingData createRoleBinding(@NotNull RoleBindingData data) {
        return api.createRoleBinding(data);
    }

    /**
     * Gets a role binding by ID.
     *
     * @param id the role binding ID.
     * @return the role binding.
     */
    public RoleBindingData getRoleBinding(@NotNull String id) {
        return api.getRoleBinding(id);
    }

    /**
     * Deletes a role binding by ID.
     *
     * @param id the role binding ID.
     */
    public void deleteRoleBinding(@NotNull String id) {
        api.deleteRoleBinding(id);
    }

    /**
     * Lists all users, handling pagination.
     *
     * @return all users.
     */
    public List<UserData> listUsers() {
        List<UserData> allUsers = new ArrayList<>();
        String pageToken = null;
        do {
            UserListResponse response = api.listUsers(DEFAULT_PAGE_SIZE, pageToken);
            if (response.data() != null) {
                allUsers.addAll(response.data());
            }
            pageToken = response.metadata() != null ? response.metadata().pageToken() : null;
        } while (pageToken != null);
        return allUsers;
    }

    /**
     * Lists all service accounts, handling pagination.
     *
     * @return all service accounts.
     */
    public List<ServiceAccountData> listServiceAccounts() {
        List<ServiceAccountData> allAccounts = new ArrayList<>();
        String pageToken = null;
        do {
            ServiceAccountListResponse response = api.listServiceAccounts(DEFAULT_PAGE_SIZE, pageToken);
            if (response.data() != null) {
                allAccounts.addAll(response.data());
            }
            pageToken = response.metadata() != null ? response.metadata().pageToken() : null;
        } while (pageToken != null);
        return allAccounts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        api.close();
    }
}
