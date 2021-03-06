package com.foxless.godfs.common;

import com.foxless.godfs.bean.Meta;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.bean.meta.OperationValidationRequest;
import com.foxless.godfs.handler.ValidateConnectionHandler;
import com.foxless.godfs.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * connection manager as a bridge.
 * manage data transfer and connection validation.
 *
 * @author hehety
 * @sine 1.0
 * @date 2018/09/26
 * @version 1.0
 */
public class Bridge {

    private static final Logger log = LoggerFactory.getLogger(Bridge.class);

    private Long index;
    private Socket connection;
    private String uuid;
    private byte[] buffer = new byte[Const.HeaderSize];

    public Bridge() {
    }

    public Bridge(Socket connection, long index) {
        this.connection = connection;
        this.index = index;
    }

    public void close() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Socket GetConn() {
        return this.connection;
    }

    /**
     * send request to server
     * @param operation operation defined in {@link com.foxless.godfs.common.Const}
     * @param operationMeta custom request meta info defined in package {@link com.foxless.godfs.bean.meta}
     * @param bodyLen request body length, if request contains no file, bodyLen = 0.
     * @param bodyWriterHandler if request contains file, you need send the bytes by your own through interface {@link IWriter}.
     * @throws Exception
     */
    public synchronized void sendRequest(int operation, Object operationMeta, long bodyLen, IWriter bodyWriterHandler) throws Exception {
        Meta meta = Utils.createMeta(operation, operationMeta, bodyLen);
        if (meta.getError() != null) {
            throw meta.getError();
        }

        byte[] metaLenBytes = convertLen2Bytes(meta.getMetaLength());
        byte[] bodyLenBytes = convertLen2Bytes(meta.getBodyLength());

        ByteBuffer headerBuff = ByteBuffer.allocate(18 + (int) meta.getMetaLength());
        headerBuff.put(Const.getOperationHeadBytes(meta.getOperation()));
        headerBuff.put(metaLenBytes);
        headerBuff.put(bodyLenBytes);
        headerBuff.put(meta.getMetaBody());

        try {
            this.connection.getOutputStream().write(headerBuff.array());
            if (meta.getBodyLength() > 0 && null != bodyWriterHandler) {
                bodyWriterHandler.write(this.connection.getOutputStream(), bodyLen);
            }
        } catch (Exception e) {
            this.close();
            throw e;
        }
    }

    /**
     * receive response after send request.
     * @param responseHandler the handler for this response
     * @throws Exception if there is something error occurs, the connection will be close.
     */
    public synchronized Object receiveResponse(Tracker tracker, Class<? extends IResponseHandler> responseHandler, IReader byteReceiver) throws Exception {
        try {
            Meta meta = readHeadMeta();
            if (null != responseHandler) {
                IResponseHandler handler = Handlers.getHandler(responseHandler);
                return handler.handle(this, tracker, meta, byteReceiver);
            }
            return null;
        } catch (Exception e) {
            this.close();
            throw e;
        }
    }

    public void validateConnection(String secret) throws Exception {

        OperationValidationRequest validateMeta = new OperationValidationRequest();
        validateMeta.setSecret(secret);
        validateMeta.setUuid("JAVA-CLIENT-0.1.0");

        this.sendRequest(Const.O_CONNECT, validateMeta, 0, null);
        this.receiveResponse(null, ValidateConnectionHandler.class, null);
    }


    // read 18 head bytes.
    private Meta readHeadMeta() throws Exception {
        // read header meta data
        readBytes(this.buffer, Const.HeaderSize);
        int operation = retrieveOperation();
        // read meta and body size
        byte[] bMetaSize = copyByteRange(2, 10);
        byte[] bBodySize = copyByteRange(10, 18);
        long metaSize = convertBytes2Len(bMetaSize);
        long bodySize = convertBytes2Len(bBodySize);
        byte[] metaBodyBytes = readMetaBytes((int) metaSize);
        Meta meta = new Meta();
        meta.setOperation(operation);
        meta.setMetaLength(metaSize);
        meta.setBodyLength(bodySize);
        meta.setMetaBody(metaBodyBytes);
        meta.setError(null);
        return meta;
    }



    // 读取meta字节信息
    private byte[] readMetaBytes(int metaSize) throws Exception {
        byte[] tmp = new byte[metaSize];
        int len = readBytes(tmp, metaSize);
        if (len == metaSize) {
            return tmp;
        }
        throw new IOException("error read sufficient bytes from connection.");
    }

    // retrieve operation code from operation head bytes.
    // return 0 if no operation code matches.
    private int retrieveOperation() {
        byte[] op = copyByteRange(0, 1);
        return Const.getOperationByHeadBytes(op);
    }

    private byte[] copyByteRange(int start, int end) {
        byte[] bs = new byte[end - start];
        for (int i = start; i < end; i++) {
            bs[i-start] = this.buffer[i];
        }
        return bs;
    }



    // 通用字节读取函数，如果读取结束/失败自动关闭连接
    // ioinout bool, true is in and false is out
    private int readBytes(byte[] buff, int len) throws IOException {
        InputStream in = this.connection.getInputStream();
        int read = 0;
        for (;;) {
            if (read >= len) {
                break;
            }
            int l = in.read(buff, read, len - read);
            if (l == 0) {
                throw new IOException("error read byte from connection.");
            }
            if (l <= len) {
                read += l;
                continue;
            }
        }
        return len;
    }

    private byte[] convertLen2Bytes(long len) {
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


    public Socket getConnection() {
        return connection;
    }

    public void setConnection(Socket connection) {
        this.connection = connection;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }
}
