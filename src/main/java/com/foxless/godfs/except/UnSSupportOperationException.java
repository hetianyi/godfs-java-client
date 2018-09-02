package com.foxless.godfs.except;

public class UnSSupportOperationException extends IllegalStateException {
    public UnSSupportOperationException() {
    }

    public UnSSupportOperationException(String s) {
        super(s);
    }

    public UnSSupportOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnSSupportOperationException(Throwable cause) {
        super(cause);
    }
}
