package com.foxless.godfs.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class StreamResolver {

    private static final Logger logger = LoggerFactory.getLogger(StreamResolver.class);

    // ReadBytes common bytes reader, if error occurs, it will close automatically
    public static int readBytes(byte[] buff, int len, ConnectionManager manager) throws IOException {
        int read = 0;
        for (;;) {
            if (read >= len) {
                break;
            }
            int read1 = manager.getConn().getInputStream().read(buff, read, len - read);
            if (read1 <= len - read) {
                read += read1;
                continue;
            }
        }
        return len;
    }

    // readFrame read frame from server/client.
    // if frame containers body, then should read it later in custom handlers.
    public static Frame readFrame(ConnectionManager manager) throws IOException {
        byte[] headerBytes = new byte[Commons.FRAME_HEAD_SIZE];
        // read header meta data
        readBytes(headerBytes, Commons.FRAME_HEAD_SIZE, manager);
        // read meta and body size
        byte[] bFrameHead = Arrays.copyOfRange(headerBytes, 0, 2);
        byte[] bMetaSize = Arrays.copyOfRange(headerBytes, 3, 11);
        byte[] bBodySize = Arrays.copyOfRange(headerBytes, 11, 19);
        long metaLength = convertBytes2Len(bMetaSize);
        long bodyLength = convertBytes2Len(bBodySize);
        byte[] metaBodyBytes = readFrameMeta((int)metaLength, manager);

        Frame frame = new Frame(bFrameHead, headerBytes[2], (int)metaLength, bodyLength, metaBodyBytes);

        if (frame.getFrameStatus() != Commons.STATUS_SUCCESS) {
            throw new IllegalStateException("server response error code "
                    + frame.getFrameStatus() + " (" + Commons.translateResponseMsg(frame.getFrameStatus()) + ")");
        }
        return frame;
    }

    // writeFrame write frame to server/client.
    public static void writeFrame(ConnectionManager manager, Frame frame) throws IOException {
        // prepare frame meta
        byte[] tmpBuf = new byte[8];
        ByteBuffer headerBuff = ByteBuffer.allocate(Commons.FRAME_HEAD_SIZE + frame.getFrameMeta().length);
        if (frame.getOperation() == Commons.FRAME_OPERATION_NONE){
            frame.setOperation(Commons.FRAME_OPERATION_NONE);
        }
        headerBuff.put(frame.getFrameHead());
        headerBuff.put(frame.getFrameStatus());
        byte[] metaLenBytes = convertLen2Bytes(frame.getMetaLength());
        byte[] bodyLenBytes = convertLen2Bytes(frame.getBodyLength());
        headerBuff.put(metaLenBytes);
        headerBuff.put(bodyLenBytes);
        headerBuff.put(frame.getFrameMeta());

        byte[] bs = headerBuff.array();
        // write frame meta
        try {
            manager.getConn().getOutputStream().write(bs);
            IHandler bodyWriterHandler = frame.getBodyWriterHandler();
            if (frame.getBodyLength() > 0 && bodyWriterHandler != null) {
                bodyWriterHandler.resolve(manager, frame);
            }
        } catch (IOException e) {
            manager.destroy();
            throw e;
        }
    }

    // readFrameMeta reads frame's meta info
    public static byte[] readFrameMeta(int metaSize, ConnectionManager manager) throws IOException {
        byte[] tmp = new byte[metaSize];
        readBytes(tmp, metaSize, manager);
        // should never happen, mark as broken connection
        return tmp;
    }


    private static byte[] convertLen2Bytes(long len) {
        byte[] bs = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = 64 - (i + 1) * 8;
            bs[i] = (byte) ((len >> offset) & 0xff);
        }
        return bs;
    }
    public static long convertBytes2Len(byte[] bs) {
        long  values = 0;
        for (int i = 0; i < 8; i++) {
            values <<= 8; values|= (bs[i] & 0xff);
        }
        return values;
    }

}
