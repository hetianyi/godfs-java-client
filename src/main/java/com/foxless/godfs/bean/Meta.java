package com.foxless.godfs.bean;

public class Meta {
    private int operation;
    private long metaLength;
    private long BodyLength;
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

    public long getBodyLength() {
        return BodyLength;
    }

    public void setBodyLength(long bodyLength) {
        BodyLength = bodyLength;
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
}
