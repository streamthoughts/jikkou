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
package io.streamthoughts.jikkou.kafka.reporter.ce;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.time.ZonedDateTime;
import java.util.Objects;

@Reflectable
@JsonPropertyOrder({
        "specversion",
        "type",
        "source",
        "id",
        "time",
        "datacontenttype",
        "subject",
})
public class CloudEventAttributes {

    /** Identifies the event. */
    protected String id;

    /** Identifies the context in which an event happened. */
    protected String source;

    /** Identifies the subject of the event. */
    protected String subject;

    /** The version of the CloudEvents specification which the event uses. */
    protected String specVersion;
    /**
     * This attribute contains a value describing the type of event related to the originating
     * occurrence.
     */
    protected String type;
    /** Timestamp of when the occurrence happened. */
    protected ZonedDateTime time;
    /** Content type of data value. */
    protected String dataContentType;

    CloudEventAttributes() {}

    @JsonProperty("id")
    public String id() {
        return id;
    }

    @JsonProperty("source")
    public String source() {
        return source;
    }

    @JsonProperty("subject")
    public String subject() {
        return subject;
    }

    @JsonProperty("specversion")
    public String specVersion() {
        return specVersion;
    }

    @JsonProperty("type")
    public String type() {
        return type;
    }

    @JsonProperty("time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    public ZonedDateTime time() {
        return time;
    }

    @JsonProperty("datacontenttype")
    public String dataContentType() {
        return dataContentType;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CloudEventAttributes)) return false;
        CloudEventAttributes that = (CloudEventAttributes) o;
        return Objects.equals(id, that.id)
                && Objects.equals(source, that.source)
                && Objects.equals(subject, that.subject)
                && Objects.equals(specVersion, that.specVersion)
                && Objects.equals(type, that.type)
                && Objects.equals(time, that.time)
                && Objects.equals(dataContentType, that.dataContentType);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(id, source, subject, specVersion, type, time, dataContentType);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "CloudEventsAttributes{"
                + "id='"
                + id
                + '\''
                + ", source='"
                + source
                + '\''
                + ", subject='"
                + subject
                + '\''
                + ", specVersion='"
                + specVersion
                + '\''
                + ", type='"
                + type
                + '\''
                + ", time="
                + time
                + ", dataContentType='"
                + dataContentType
                + '\''
                + '}';
    }
}
