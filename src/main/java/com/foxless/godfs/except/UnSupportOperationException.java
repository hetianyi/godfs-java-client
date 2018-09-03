package com.foxless.godfs.except;

public class UnSupportOperationException extends IllegalStateException {
    public UnSupportOperationException() {
    }

    public UnSupportOperationException(String s) {
        super(s);
    }

    public UnSupportOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnSupportOperationException(Throwable cause) {
        super(cause);
    }
}
