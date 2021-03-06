package com.foxless.godfs.common;

import com.foxless.godfs.config.ClientConfigurationBean;

import java.util.HashMap;
import java.util.Map;

public class Const {

    public static final int STATUS_OK = 0;
    public static final int STATUS_BAD_SECRET = 1;
    public static final int STATUS_OPERATION_NOT_SUPPORT = 2;
    public static final int STATUS_INTERNAL_SERVER_ERROR = 3;
    public static final int STATUS_NOT_FOUND = 4;


    public static final int O_CONNECT = 1;
    public static final int O_RESPONSE = 2;

    public static final int O_QUERY_FILE = 3;
    public static final int O_DOWNLOAD_FILE = 4;
    public static final int O_REG_STORAGE = 5;
    public static final int O_REG_FILE = 6;
    public static final int O_SYNC_STORAGE = 7;
    public static final int O_PULL_NEW_FILES = 8;
    public static final int O_UPLOAD = 9;
    public static final int O_SYNC_MEMBERS = 10;

    public static final int HeaderSize = 18;

    public static final String PATH_REGEX = "([0-9a-zA-Z_]{1,10})/([0-9a-zA-Z_]{1,10})/([MS])/([0-9a-fA-F]{32})";
    public static final String MD5_REGEX = "[0-9a-fA-F]{32}";

    // TODO temp
    public static final int MAX_CONN_EACH_STORAGE_SERVER = 10;
    public static final int BUFFER_SIZE = 10240;


    private static IPool pool;

    private static Map<Integer, byte[]> operationHeadMap = new HashMap<Integer, byte[]>(10);

    static {
        operationHeadMap.put(O_CONNECT, new byte[]{1,1});
        operationHeadMap.put(O_RESPONSE, new byte[]{1,2});
        operationHeadMap.put(O_UPLOAD, new byte[]{1,3});
        operationHeadMap.put(O_QUERY_FILE, new byte[]{1,4});
        operationHeadMap.put(O_DOWNLOAD_FILE, new byte[]{1,5});
        operationHeadMap.put(O_REG_STORAGE, new byte[]{1,6});
        operationHeadMap.put(O_REG_FILE, new byte[]{1,7});
        operationHeadMap.put(O_SYNC_STORAGE, new byte[]{1,8});
        operationHeadMap.put(O_PULL_NEW_FILES, new byte[]{1,9});
        operationHeadMap.put(O_SYNC_MEMBERS, new byte[]{1,10});
    }

    public static boolean containsOperationCode(int operation) {
        return operationHeadMap.containsKey(operation);
    }

    public static byte[] getOperationHeadBytes(int operation) {
        return operationHeadMap.get(operation);
    }

    public static int getOperationByHeadBytes(byte[] op) {
        for (Map.Entry<Integer, byte[]> entry : operationHeadMap.entrySet()) {
            if (entry.getValue().equals(op)) {
                return entry.getKey();
            }
        }
        return 0;
    }

    /**
     * initial connection pool.
     * @param configuration
     */
    public static synchronized void initPool(ClientConfigurationBean configuration) {
        if (null != pool) {
            return;
        }
        pool = new ConnectionPool();
        pool.initPool(configuration);
    }

    public static IPool getPool() {
        return pool;
    }
}
