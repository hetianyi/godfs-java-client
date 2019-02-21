package com.foxless.godfs.bridge;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxless.godfs.util.Utils;

public class Frame {
    // FrameHeadFlag frame head
    public static final byte FRAME_HEAD_FLAG = 66;

    private byte[] frameHead;
    private byte frameStatus;
    private int metaLength;
    private long bodyLength;
    private byte[] frameMeta;
    private IHandler bodyWriterHandler;

    public Frame() {
    }

    public Frame(byte[] frameHead, byte frameStatus, int metaLength, long bodyLength, byte[] frameMeta) {
        this.frameHead = frameHead;
        this.frameStatus = frameStatus;
        this.metaLength = metaLength;
        this.bodyLength = bodyLength;
        this.frameMeta = frameMeta;
    }

    // SetOperation set operation code of the frame
    public void setOperation(byte operation) {
        this.frameHead = new byte[]{FRAME_HEAD_FLAG, operation};
    }

    // GetOperation get operation code of the frame
    public byte getOperation()  {
        if (this.frameHead == null || this.frameHead.length != 2) {
            return Commons.FRAME_OPERATION_NONE;
        }
        return this.frameHead[1];
    }

    // SetStatus set frame's status
    public void SetStatus(byte status) {
        this.frameStatus = status;
    }

    // GetStatus get frame's status
    public byte  getStatus()  {
        return this.frameStatus;
    }

    // SetMeta set frame's meta info
    public void setMeta(Object meta) throws JsonProcessingException {
        if (meta == null) {
            throw new IllegalArgumentException("cannot set frame meta to null");
        }
        ObjectMapper objectMapper = Utils.getObjectMapper();
        String metaStr = objectMapper.writeValueAsString(meta);
        this.frameMeta = metaStr.getBytes();
        this.metaLength = this.frameMeta.length;
    }

    // GetMeta get frame's meta info
    public byte[] getMeta() {
        return this.frameMeta;
    }

    // SetMetaBodyLength set frame's body length
    public void setMetaBodyLength(long bodyLength) {
        this.bodyLength = bodyLength;
    }


    // -------------getters and setters-------------

    public byte[] getFrameHead() {
        return frameHead;
    }

    public void setFrameHead(byte[] frameHead) {
        this.frameHead = frameHead;
    }

    public byte getFrameStatus() {
        return frameStatus;
    }

    public void setFrameStatus(byte frameStatus) {
        this.frameStatus = frameStatus;
    }

    public int getMetaLength() {
        return metaLength;
    }

    public void setMetaLength(int metaLength) {
        this.metaLength = metaLength;
    }

    public long getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(long bodyLength) {
        this.bodyLength = bodyLength;
    }

    public byte[] getFrameMeta() {
        return frameMeta;
    }

    public void setFrameMeta(byte[] frameMeta) {
        this.frameMeta = frameMeta;
    }

    public IHandler getBodyWriterHandler() {
        return bodyWriterHandler;
    }

    public void setBodyWriterHandler(IHandler bodyWriterHandler) {
        this.bodyWriterHandler = bodyWriterHandler;
    }
}
