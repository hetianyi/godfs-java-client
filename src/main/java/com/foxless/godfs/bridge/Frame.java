package com.foxless.godfs.bridge;




public class Frame {
    private byte[] frameHead;
    private byte FrameStatus;
    private int MetaLength;
    private long BodyLength;
    private byte[] FrameMeta;
    private byte[] BodyWriterHandler;
}
