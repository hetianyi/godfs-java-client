package com.foxless.godfs.bean;

public class Meta {
    private int operation;
    private long metaLength;
    private long bodyLength;
    private byte[] metaBody;
    private Exception error;

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public long getMetaLength() {
        return metaLength;
    }

    public void setMetaLength(long metaLength) {
        this.metaLength = metaLength;
    }

    public byte[] getMetaBody() {
        return metaBody;
    }

    public void setMetaBody(byte[] metaBody) {
        this.metaBody = metaBody;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }

    public long getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(long bodyLength) {
        this.bodyLength = bodyLength;
    }
}
