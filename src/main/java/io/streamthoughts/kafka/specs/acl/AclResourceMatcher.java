package io.streamthoughts.kafka.specs.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.Objects;
import java.util.regex.Pattern;

public class AclResourceMatcher {

    private static final String WILDCARD = "*";
    private static final Pattern LITERAL = Pattern.compile("[a-zA-Z0-9\\._\\-]+");

    private final String pattern;
    private final PatternType patternType;
    private final ResourceType type;

    @JsonCreator
    public AclResourceMatcher(final @JsonProperty("pattern") String pattern,
                              final @JsonProperty("pattern_type") PatternType patternType,
                              final @JsonProperty("type") ResourceType type) {
        this.pattern = pattern;
        this.patternType = patternType;
        this.type = type;
        validate();
    }

    private void validate() {
        if (PatternType.LITERAL.equals(patternType) && type.equals(ResourceType.TOPIC)) {
            if ( !(LITERAL.matcher(pattern).matches() || pattern.equals(WILDCARD)) ) {
                throw new IllegalArgumentException("This literal pattern for topic resource is not supported: " + pattern);
            }
        }
    }

    @JsonProperty("pattern")
    public String pattern() {
        return pattern;
    }

    @JsonProperty("pattern_type")
    public PatternType patternType() {
        return patternType;
    }

    @JsonProperty("type")
    public ResourceType type() {
        return type;
    }

    public boolean isPatternOfTypeMatchRegex() {
        return this.patternType == PatternType.MATCH
                && this.pattern.startsWith("/")
                && this.pattern.endsWith("/");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AclResourceMatcher)) return false;
        AclResourceMatcher that = (AclResourceMatcher) o;
        return Objects.equals(pattern, that.pattern) &&
                patternType == that.patternType &&
                type == that.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(pattern, patternType, type);
    }
}
