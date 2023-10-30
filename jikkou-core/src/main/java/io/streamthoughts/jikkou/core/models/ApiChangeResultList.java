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
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.reconcilier.Change;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotNull;

/**
 * ReconciliationChangeResultList.
 */
@Kind("ApiChangeResultList")
@ApiVersion("core.jikkou.io/v1")
@JsonPropertyOrder({
        "kind",
        "apiVersion",
        "dryRun",
        "metadata",
        "changes"
})
@Reflectable
@JsonDeserialize
public class ApiChangeResultList {
    private final String kind;
    private final String apiVersion;
    private final boolean dryRun;
    private final ObjectMeta metadata;
    private final List<ChangeResult<Change>> changes;

    /**
     * Creates a new {@link ApiChangeResultList} instance.
     */
    @ConstructorProperties({
            "kind",
            "apiVersion",
            "dryRun",
            "metadata",
            "changes"
    })
    public ApiChangeResultList(@NotNull String kind,
                               @NotNull String apiVersion,
                               boolean dryRun,
                               @NotNull ObjectMeta metadata,
                               @NotNull List<? extends ChangeResult<Change>> changes) {
        this.kind = kind;
        this.apiVersion = apiVersion;
        this.dryRun = dryRun;
        this.metadata = metadata;
        this.changes = Collections.unmodifiableList(changes);
    }

    /**
     * Creates a new {@link ApiChangeResultList} instance.
     *
     * @param dryRun  specify whether teh reconciliation have benn executed in dry-run.
     * @param changes list of change result.
     */
    public ApiChangeResultList(boolean dryRun,
                               List<ChangeResult<Change>> changes) {
        this(
                Resource.getKind(ApiChangeResultList.class),
                Resource.getApiVersion(ApiChangeResultList.class),
                dryRun,
                new ObjectMeta(),
                changes
        );
    }

    /**
     * Creates a new {@link ApiChangeResultList} instance.
     *
     * @param dryRun  specify whether teh reconciliation have benn executed in dry-run.
     * @param changes list of change result.
     */
    public ApiChangeResultList(boolean dryRun,
                               ObjectMeta metadata,
                               List<ChangeResult<Change>> changes) {
        this(
                Resource.getKind(ApiChangeResultList.class),
                Resource.getApiVersion(ApiChangeResultList.class),
                dryRun,
                metadata,
                changes
        );
    }

    @JsonProperty("kind")
    public @NotNull String getKind() {
        return kind;
    }

    @JsonProperty("apiVersion")
    public @NotNull String getApiVersion() {
        return apiVersion;
    }

    @JsonProperty("dryRun")
    public boolean isDryRun() {
        return dryRun;
    }

    @JsonProperty("metadata")
    public ObjectMeta getMetadata() {
        ObjectMeta objectMeta = Optional.ofNullable(metadata).orElse(new ObjectMeta());
        return objectMeta.toBuilder()
                .withAnnotation(CoreAnnotations.JIKKOU_IO_CHANGE_COUNT, changes.size())
                .build();
    }

    @JsonProperty("changes")
    public @NotNull List<ChangeResult<Change>> getChanges() {
        return changes;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ApiChangeResultList) obj;
        return Objects.equals(this.kind, that.kind) &&
                Objects.equals(this.apiVersion, that.apiVersion) &&
                this.dryRun == that.dryRun &&
                Objects.equals(this.metadata, that.metadata) &&
                Objects.equals(this.changes, that.changes);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(kind, apiVersion, dryRun, metadata, changes);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "ReconciliationChangeResultList[" +
                "kind=" + kind + ", " +
                "apiVersion=" + apiVersion + ", " +
                "dryRun=" + dryRun + ", " +
                "metadata=" + metadata + ", " +
                "changes=" + changes + ']';
    }
}
