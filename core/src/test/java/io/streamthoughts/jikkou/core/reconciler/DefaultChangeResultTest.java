package io.streamthoughts.jikkou.core.reconciler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.change.DefaultTextDescription;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import org.junit.jupiter.api.Test;

class DefaultChangeResultTest {

    @Test
    void shouldProperlyChangeResult() {
        // Given
        ChangeResult change = ChangeResult.ok(GenericResourceChange.builder().build(), new DefaultTextDescription("testing change"));
        ObjectMapper objectMapper = Jackson.JSON_OBJECT_MAPPER;

        // Then
        assertDoesNotThrow(() -> {
            // When
            String changeAsString = objectMapper.writeValueAsString(change);
            assertNotNull(changeAsString);
        });
    }

}