package com.foxless.godfs.bridge;

public class OperationHandler {
    private byte operationCode;
    private IHandler handler;

    public byte getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(byte operationCode) {
        this.operationCode = operationCode;
    }

    public IHandler getHandler() {
        return handler;
    }

    public void setHandler(IHandler handler) {
        this.handler = handler;
    }
}
