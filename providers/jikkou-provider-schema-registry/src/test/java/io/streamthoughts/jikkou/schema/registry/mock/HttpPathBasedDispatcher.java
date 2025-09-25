package io.streamthoughts.jikkou.schema.registry.mock;

import java.util.HashMap;
import java.util.Map;
import mockwebserver3.Dispatcher;
import mockwebserver3.MockResponse;
import mockwebserver3.RecordedRequest;
import okhttp3.Headers;
import org.jetbrains.annotations.NotNull;

public class HttpPathBasedDispatcher extends Dispatcher {

    private final Map<String, MockResponse> responses;

    public HttpPathBasedDispatcher(Map<String, MockResponse> responses) {
        this.responses = responses;
    }

    public static HTTPPathBasedDispatcherBuilder builder() {
        return new HTTPPathBasedDispatcherBuilder();
    }

    @Override
    public @NotNull MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
        String path = recordedRequest.getUrl().url().getPath();
        return responses.getOrDefault(path, new MockResponse(404, Headers.EMPTY, ""));
    }

    public static class HTTPPathBasedDispatcherBuilder {
        private final Map<String, MockResponse> responses = new HashMap<>();

        public HTTPPathBasedDispatcherBuilder forPath(String path, MockResponse response) {
            responses.put(path, response);
            return this;
        }

        public HttpPathBasedDispatcher build() {
            return new HttpPathBasedDispatcher(responses);
        }
    }
}
