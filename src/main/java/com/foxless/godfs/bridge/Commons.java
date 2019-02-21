package com.foxless.godfs.bridge;

import com.foxless.godfs.pool.ClientConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Commons {

    private static final Logger logger = LoggerFactory.getLogger(Commons.class);

    public static final int FRAME_HEAD_SIZE = 19;
    public static final String UUID = "JAVA-CLIENT.v1.0.5";

    public static final byte STATUS_SUCCESS = 1;
    public static final byte STATUS_INTERNAL_SERVER_ERROR = 2;
    public static final byte STATUS_BAD_SECRET = 3;
    public static final byte STATUS_FULL_CONNECTION_POOL = 4;
    public static final byte STATUS_INSTANCE_ID_EXIST = 5;

    public static final byte FRAME_OPERATION_NONE = 0;
    public static final byte FRAME_OPERATION_VALIDATE = 2;
    public static final byte FRAME_OPERATION_SYNC_STORAGE_MEMBER = 4;
    public static final byte FRAME_OPERATION_REGISTER_FILES = 5;
    public static final byte FRAME_OPERATION_SYNC_ALL_STORAGE_SERVERS = 6;
    public static final byte FRAME_OPERATION_PULL_NEW_FILES = 7;
    public static final byte FRAME_OPERATION_SYNC_STATISTICS = 8;
    public static final byte FRAME_OPERATION_QUERY_FILES = 9;
    public static final byte FRAME_OPERATION_UPLOAD_FILE = 10;
    public static final byte FRAME_OPERATION_DOWNLOAD_FILE = 11;

    public static final int SERVER_SIDE = 1;
    public static final int CLIENT_SIDE = 2;

    public static final int STATE_NOT_CONNECT = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_VALIDATED = 2;
    public static final int STATE_DISCONNECTED = 3;

    public static final Map<Byte, OperationHandler> operationHandlerMap = new HashMap<>();
    public static final Map<Byte, String> responseCodeMap = new HashMap<>();

    public static final ClientConnectionPool connPool = new ClientConnectionPool();

    static {
        responseCodeMap.put(STATUS_SUCCESS, "operation success");
        responseCodeMap.put(STATUS_INTERNAL_SERVER_ERROR, "internal server error");
        responseCodeMap.put(STATUS_BAD_SECRET, "bad secret");
        responseCodeMap.put(STATUS_FULL_CONNECTION_POOL, "connection pool is full");
        responseCodeMap.put(STATUS_INSTANCE_ID_EXIST, "instance id is not unique");
    }

    public static void initPool(Integer maxConnPerServer) {
        connPool.init(maxConnPerServer);
    }

    public static String translateResponseMsg(byte code) {
        return responseCodeMap.get(code);
    }


    // RegisterOperationHandler register handler dynamically from high level.
    // usually register all handlers at entry file, such as tracker.go, storage.go...
    public static void registerOperationHandler(OperationHandler handler) {
        if (handler == null) {
            return;
        }
        if (operationHandlerMap.get(handler.getOperationCode()) != null) {
            logger.warn("handler already registered:", handler.getOperationCode());
            return;
        }
        logger.debug("register operation handler:", handler.getOperationCode());
        operationHandlerMap.put(handler.getOperationCode(), handler);
    }

    // GetOperationHandler get handler by operation code
    public static OperationHandler getOperationHandler(byte operation) {
        return operationHandlerMap.get(operation);
    }

}
