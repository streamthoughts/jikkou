package io.streamthoughts.jikkou.kafka.error;

public class ConfigException extends JikkouException {

    public ConfigException() {
        super();
    }

    public ConfigException(final String message) {
        super(message);
    }

    public ConfigException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public ConfigException(final Throwable cause) {
        super(cause);
    }
}
