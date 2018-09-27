package com.foxless.godfs.except;

public class ExhaustedConnectionPoolException extends IllegalStateException {
    public ExhaustedConnectionPoolException() {
    }

    public ExhaustedConnectionPoolException(String s) {
        super(s);
    }

    public ExhaustedConnectionPoolException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExhaustedConnectionPoolException(Throwable cause) {
        super(cause);
    }
}
