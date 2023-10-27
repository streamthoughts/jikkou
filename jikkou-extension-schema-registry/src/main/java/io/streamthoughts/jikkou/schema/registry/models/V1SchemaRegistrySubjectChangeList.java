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
package io.streamthoughts.jikkou.schema.registry.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.annotation.Verbs;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ObjectTemplate;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import lombok.extern.jackson.Jacksonized;


/**
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("")
@Verbs({

})
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "template",
    "items"
})
@ApiVersion("kafka.jikkou.io/v1beta2")
@Kind("SchemaSubjectChangeList")
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1SchemaRegistrySubjectChangeList implements ResourceListObject<V1SchemaRegistrySubjectChange>
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    @Builder.Default
    private String apiVersion = "kafka.jikkou.io/v1beta2";
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    @Builder.Default
    private String kind = "SchemaSubjectChangeList";
    @JsonProperty("metadata")
    private ObjectMeta metadata;
    @JsonProperty("template")
    private ObjectTemplate template;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("items")
    @Singular
    private List<io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectChange> items = new ArrayList<io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectChange>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1SchemaRegistrySubjectChangeList() {
    }

    /**
     * 
     * @param template
     * @param metadata
     * @param apiVersion
     * @param kind
     * @param items
     */
    @ConstructorProperties({
        "apiVersion",
        "kind",
        "metadata",
        "template",
        "items"
    })
    public V1SchemaRegistrySubjectChangeList(String apiVersion, String kind, ObjectMeta metadata, ObjectTemplate template, List<io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectChange> items) {
        super();
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.metadata = metadata;
        this.template = template;
        this.items = items;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    public String getKind() {
        return kind;
    }

    @JsonProperty("metadata")
    public ObjectMeta getMetadata() {
        return metadata;
    }

    @JsonProperty("template")
    public ObjectTemplate getTemplate() {
        return template;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("items")
    public List<io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectChange> getItems() {
        return items;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1SchemaRegistrySubjectChangeList.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("apiVersion");
        sb.append('=');
        sb.append(((this.apiVersion == null)?"<null>":this.apiVersion));
        sb.append(',');
        sb.append("kind");
        sb.append('=');
        sb.append(((this.kind == null)?"<null>":this.kind));
        sb.append(',');
        sb.append("metadata");
        sb.append('=');
        sb.append(((this.metadata == null)?"<null>":this.metadata));
        sb.append(',');
        sb.append("template");
        sb.append('=');
        sb.append(((this.template == null)?"<null>":this.template));
        sb.append(',');
        sb.append("items");
        sb.append('=');
        sb.append(((this.items == null)?"<null>":this.items));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.template == null)? 0 :this.template.hashCode()));
        result = ((result* 31)+((this.metadata == null)? 0 :this.metadata.hashCode()));
        result = ((result* 31)+((this.apiVersion == null)? 0 :this.apiVersion.hashCode()));
        result = ((result* 31)+((this.items == null)? 0 :this.items.hashCode()));
        result = ((result* 31)+((this.kind == null)? 0 :this.kind.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1SchemaRegistrySubjectChangeList) == false) {
            return false;
        }
        V1SchemaRegistrySubjectChangeList rhs = ((V1SchemaRegistrySubjectChangeList) other);
        return ((((((this.template == rhs.template)||((this.template!= null)&&this.template.equals(rhs.template)))&&((this.metadata == rhs.metadata)||((this.metadata!= null)&&this.metadata.equals(rhs.metadata))))&&((this.apiVersion == rhs.apiVersion)||((this.apiVersion!= null)&&this.apiVersion.equals(rhs.apiVersion))))&&((this.items == rhs.items)||((this.items!= null)&&this.items.equals(rhs.items))))&&((this.kind == rhs.kind)||((this.kind!= null)&&this.kind.equals(rhs.kind))));
    }

}
