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
package io.streamthoughts.jikkou.kafka.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.annotation.Description;
import io.streamthoughts.jikkou.annotation.Reflectable;
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
@JsonPropertyOrder({
    "acls"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaPrincipalRoleSpec {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("acls")
    @Singular
    private List<V1KafkaPrincipalAcl> acls = new ArrayList<V1KafkaPrincipalAcl>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaPrincipalRoleSpec() {
    }

    /**
     * 
     * @param acls
     */
    @ConstructorProperties({
        "acls"
    })
    public V1KafkaPrincipalRoleSpec(List<V1KafkaPrincipalAcl> acls) {
        super();
        this.acls = acls;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("acls")
    public List<V1KafkaPrincipalAcl> getAcls() {
        return acls;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaPrincipalRoleSpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("acls");
        sb.append('=');
        sb.append(((this.acls == null)?"<null>":this.acls));
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
        result = ((result* 31)+((this.acls == null)? 0 :this.acls.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaPrincipalRoleSpec) == false) {
            return false;
        }
        V1KafkaPrincipalRoleSpec rhs = ((V1KafkaPrincipalRoleSpec) other);
        return ((this.acls == rhs.acls)||((this.acls!= null)&&this.acls.equals(rhs.acls)));
    }

}
