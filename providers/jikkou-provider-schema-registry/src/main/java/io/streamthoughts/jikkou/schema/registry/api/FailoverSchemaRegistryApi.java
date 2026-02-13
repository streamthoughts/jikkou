/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api;

import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityCheck;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityLevelObject;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityObject;
import io.streamthoughts.jikkou.schema.registry.api.data.ModeObject;
import io.streamthoughts.jikkou.schema.registry.api.data.SchemaString;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaId;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaVersion;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectVersion;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SchemaRegistryApi} decorator that provides failover across multiple Schema Registry instances.
 * On connection-related failures, the next instance in the list is tried.
 */
public final class FailoverSchemaRegistryApi implements SchemaRegistryApi {

    private static final Logger LOG = LoggerFactory.getLogger(FailoverSchemaRegistryApi.class);

    private final List<SchemaRegistryApi> delegates;

    /**
     * Creates a new {@link FailoverSchemaRegistryApi} instance.
     *
     * @param delegates the list of Schema Registry API instances to failover between.
     */
    public FailoverSchemaRegistryApi(List<SchemaRegistryApi> delegates) {
        if (delegates == null || delegates.isEmpty()) {
            throw new IllegalArgumentException("At least one SchemaRegistryApi instance is required");
        }
        this.delegates = List.copyOf(delegates);
    }

    private <T> T executeWithFailover(Function<SchemaRegistryApi, T> action) {
        RuntimeException lastException = null;
        for (int i = 0; i < delegates.size(); i++) {
            SchemaRegistryApi delegate = delegates.get(i);
            try {
                return action.apply(delegate);
            } catch (RuntimeException e) {
                if (isConnectionFailure(e)) {
                    LOG.warn("Connection failed to Schema Registry instance [{}], trying next instance", i, e);
                    lastException = e;
                } else {
                    throw e;
                }
            }
        }
        throw lastException;
    }

    /**
     * Determines if an exception represents a connection failure
     * by checking the cause chain for {@link IOException}.
     */
    private static boolean isConnectionFailure(RuntimeException e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof IOException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    public Response get() {
        return executeWithFailover(SchemaRegistryApi::get);
    }

    @Override
    public List<String> listSubjects() {
        return executeWithFailover(SchemaRegistryApi::listSubjects);
    }

    @Override
    public List<Integer> deleteSubjectVersions(String subject, boolean permanent) {
        return executeWithFailover(api -> api.deleteSubjectVersions(subject, permanent));
    }

    @Override
    public List<Integer> getAllSubjectVersions(String subject) {
        return executeWithFailover(api -> api.getAllSubjectVersions(subject));
    }

    @Override
    public SubjectSchemaId registerSchema(String subject, SubjectSchemaRegistration schema, boolean normalize) {
        return executeWithFailover(api -> api.registerSchema(subject, schema, normalize));
    }

    @Override
    public SubjectSchemaVersion checkSubjectVersion(String subject, SubjectSchemaRegistration schema, boolean normalize) {
        return executeWithFailover(api -> api.checkSubjectVersion(subject, schema, normalize));
    }

    @Override
    public SubjectSchemaVersion getLatestSubjectSchema(String subject) {
        return executeWithFailover(api -> api.getLatestSubjectSchema(subject));
    }

    @Override
    public SubjectSchemaVersion getSchemaByVersion(String subject, int version) {
        return executeWithFailover(api -> api.getSchemaByVersion(subject, version));
    }

    @Override
    public List<String> getSchemasTypes() {
        return executeWithFailover(SchemaRegistryApi::getSchemasTypes);
    }

    @Override
    public SchemaString getSchemaById(String id) {
        return executeWithFailover(api -> api.getSchemaById(id));
    }

    @Override
    public String getSchemaOnlyById(String id) {
        return executeWithFailover(api -> api.getSchemaOnlyById(id));
    }

    @Override
    public List<SubjectVersion> getVersionSchemaById(String id) {
        return executeWithFailover(api -> api.getVersionSchemaById(id));
    }

    @Override
    public CompatibilityLevelObject getGlobalCompatibility() {
        return executeWithFailover(SchemaRegistryApi::getGlobalCompatibility);
    }

    @Override
    public CompatibilityLevelObject getConfigCompatibility(String subject, boolean defaultToGlobal) {
        return executeWithFailover(api -> api.getConfigCompatibility(subject, defaultToGlobal));
    }

    @Override
    public CompatibilityObject updateConfigCompatibility(String subject, CompatibilityObject compatibility) {
        return executeWithFailover(api -> api.updateConfigCompatibility(subject, compatibility));
    }

    @Override
    public CompatibilityObject deleteConfigCompatibility(String subject) {
        return executeWithFailover(api -> api.deleteConfigCompatibility(subject));
    }

    @Override
    public ModeObject getMode() {
        return executeWithFailover(SchemaRegistryApi::getMode);
    }

    @Override
    public ModeObject getMode(String subject) {
        return executeWithFailover(api -> api.getMode(subject));
    }

    @Override
    public ModeObject updateMode(String subject, ModeObject mode) {
        return executeWithFailover(api -> api.updateMode(subject, mode));
    }

    @Override
    public ModeObject deleteMode(String subject) {
        return executeWithFailover(api -> api.deleteMode(subject));
    }

    @Override
    public CompatibilityCheck testCompatibility(String subject, int version, boolean verbose,
                                                 SubjectSchemaRegistration schema) {
        return executeWithFailover(api -> api.testCompatibility(subject, version, verbose, schema));
    }

    @Override
    public CompatibilityCheck testCompatibilityLatest(String subject, boolean verbose,
                                                       SubjectSchemaRegistration schema) {
        return executeWithFailover(api -> api.testCompatibilityLatest(subject, verbose, schema));
    }

    @Override
    public void close() {
        for (SchemaRegistryApi delegate : delegates) {
            delegate.close();
        }
    }
}
