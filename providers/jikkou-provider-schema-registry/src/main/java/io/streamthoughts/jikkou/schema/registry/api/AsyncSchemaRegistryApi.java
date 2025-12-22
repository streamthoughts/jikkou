/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api;

import io.streamthoughts.jikkou.schema.registry.api.data.*;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

/**
 * Asynchronous Schema Registry Api.
 */
public interface AsyncSchemaRegistryApi extends AutoCloseable {

    /**
     * Gets a list of registered subjects.
     *
     * @return a list of registered subjects.
     */
    Mono<List<String>> listSubjects();

    /**
     * Deletes the specified subject and its associated compatibility level if registered.
     *
     * @param subject   the name of the subject
     * @param permanent flag to specify a hard delete of the subject, which removes
     *                  all associated metadata including the schema ID.
     * @return a list of versions
     */
    Mono<List<Integer>> deleteSubjectVersions(@NotNull String subject,
                                              boolean permanent);

    /**
     * Register a new schema under the specified subject.
     *
     * @param subject   the name of the subject.
     * @param schema    the schema to be registered.
     * @param normalize whether to normalize the given schema
     * @return the globally unique identifier of the schema.
     */
    Mono<SubjectSchemaId> registerSubjectVersion(@NotNull String subject,
                                                 @NotNull SubjectSchemaRegistration schema,
                                                 boolean normalize);

    /**
     * Get the latest version of the schema registered under the specified subject.
     *
     * @param subject name of the subject
     * @return a {@link SubjectSchemaVersion} object.
     */
    Mono<SubjectSchemaVersion> getLatestSubjectSchema(@NotNull String subject);

    /**
     * Gets the schema registry global compatibility level.
     *
     * @return the compatibility level.
     */
    Mono<CompatibilityLevelObject> getGlobalCompatibility();

    /**
     * Gets compatibility level for the specified subject.
     *
     * @param subject         the name of the subject.
     * @param defaultToGlobal flag to default to global compatibility.
     * @return the compatibility level.
     */
    Mono<CompatibilityLevelObject> getSubjectCompatibilityLevel(@NotNull String subject,
                                                                boolean defaultToGlobal);

    /**
     * Updates compatibility level for the specified subject.
     *
     * @param subject       the name of the subject.
     * @param compatibility the new compatibility level for the subject.
     * @return the updated compatibility level.
     */
    Mono<CompatibilityObject> updateSubjectCompatibilityLevel(@NotNull String subject,
                                                              @NotNull CompatibilityObject compatibility);

    /**
     * Deletes the specified subject-level compatibility level config and reverts to the global default.
     *
     * @param subject the name of the subject.
     * @return the compatibility level.
     */
    Mono<CompatibilityObject> deleteSubjectCompatibilityLevel(@NotNull String subject);


    Mono<CompatibilityCheck> testCompatibility(@NotNull String subject,
                                               String version,
                                               boolean verbose,
                                               @NotNull SubjectSchemaRegistration schema);

    /**
     * Gets the schema registry global mode.
     *
     * @return                the mode.
     */
    Mono<ModeObject> getGlobalMode();

    /**
     * Gets mode level for the specified subject.
     *
     * @param subject         the name of the subject.
     * @return                the mode.
     */
    Mono<ModeObject> getSubjectMode(@NotNull String subject);

    /**
     * Updates mode for the specified subject.
     *
     * @param subject       the name of the subject.
     * @param mode          the new mode for the subject.
     * @return              the updated mode.
     */
    Mono<ModeObject> updateSubjectMode(@NotNull String subject, @NotNull ModeObject mode);

    /**
     * Deletes the specified subject-level mode and reverts to the global default.
     *
     * @param subject the name of the subject.
     * @return        the mode.
     */
    Mono<ModeObject> deleteSubjectMode(@NotNull String subject);

    Mono<CompatibilityCheck> testCompatibilityLatest(@NotNull String subject,
                                                     boolean verbose,
                                                     @NotNull SubjectSchemaRegistration schema);

    @Override
    default void close() {
    }
}
