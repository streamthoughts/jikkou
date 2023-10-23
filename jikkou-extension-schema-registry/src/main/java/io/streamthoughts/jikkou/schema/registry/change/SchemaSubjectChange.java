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
package io.streamthoughts.jikkou.schema.registry.change;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.change.Change;
import io.streamthoughts.jikkou.core.change.ChangeType;
import io.streamthoughts.jikkou.core.change.ValueChange;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaReference;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents a change of a subject's schema.
 */
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@AllArgsConstructor
@JsonPropertyOrder({"subject", "compatibilityLevels", "schemaType", "schema"})
@Getter
public final class SchemaSubjectChange implements Change {

    /**
     * The change-type.
     */
    private final ChangeType changeType;
    /**
     * The name of the subject.
     */
    private final String subject;
    /**
     * The schema type.
     */
    private final ValueChange<SchemaType> schemaType;
    /**
     * The string schema.
     */
    private final ValueChange<String> schema;
    /**
     * The compatibility level for this subject.
     */
    private final ValueChange<CompatibilityLevels> compatibilityLevels;
    /**
     * The references for teh schema.
     */
    private final ValueChange<List<SubjectSchemaReference>> references;
    /**
     * The options.
     */
    private final SchemaSubjectChangeOptions options;

    @JsonIgnore
    public SchemaSubjectChangeOptions getOptions() {
        return options;
    }

    @JsonProperty("operation")
    public ChangeType operation() {
        return changeType;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaSubjectChange change = (SchemaSubjectChange) o;
        return changeType == change.changeType &&
                Objects.equals(subject, change.subject) &&
                Objects.equals(schemaType, change.schemaType) &&
                Objects.equals(schema, change.schema) &&
                Objects.equals(compatibilityLevels, change.compatibilityLevels);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(changeType, subject, schemaType, schema, compatibilityLevels);
    }

    @Override
    public String toString() {
        return "SchemaSubjectChange{" +
                "changeType=" + changeType +
                ", subject='" + subject + '\'' +
                ", schemaType=" + schemaType +
                ", schema=" + schema +
                ", compatibilityLevels=" + compatibilityLevels +
                ", references=" + references +
                ", options=" + options +
                '}';
    }
}
