package com.foxless.godfs.handler;

import com.alibaba.fastjson.JSON;
import com.foxless.godfs.bean.Meta;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.bean.meta.OperationValidationResponse;
import com.foxless.godfs.common.Bridge;
import com.foxless.godfs.common.Const;
import com.foxless.godfs.common.IReader;
import com.foxless.godfs.common.IResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.Socket;

public class ValidateConnectionHandler implements IResponseHandler {

    private static final Logger log = LoggerFactory.getLogger(ValidateConnectionHandler.class);

    @Override
    public Object handle(Bridge bridge, Tracker tracker, Meta meta, IReader byteReceiver) throws Exception {
        if (meta.getError() != null) {
            throw meta.getError();
        }
        OperationValidationResponse response = JSON.parseObject(new String(meta.getMetaBody()), OperationValidationResponse.class);
        log.debug("validate response status {} from server.", response.getStatus());
        Socket connection = bridge.getConnection();
        if (response.getStatus() == Const.STATUS_OK) {
            log.info("validate success with tracker server: {}:{}", connection.getInetAddress().getHostAddress(), connection.getPort());
        } else if (response.getStatus() == Const.STATUS_BAD_SECRET) {
            log.error("validate failed with tracker server: {}:{} due to: {}", connection.getInetAddress().getHostAddress(), connection.getPort(), "STATUS_BAD_SECRET");
            throw new IllegalStateException("STATUS_BAD_SECRET");
        } else if (response.getStatus() == Const.STATUS_INTERNAL_SERVER_ERROR) {
            log.error("validate failed with tracker server: {}:{} due to: {}", connection.getInetAddress().getHostAddress(), connection.getPort(), "STATUS_INTERNAL_SERVER_ERROR");
            throw new IllegalStateException("STATUS_INTERNAL_SERVER_ERROR");
        } else {
            log.error("validate failed with tracker server: {}:{} due to: {}", connection.getInetAddress().getHostAddress(), connection.getPort(), response.getStatus());
            throw new IllegalStateException("server response unknown status code: " + response.getStatus());
        }
        return null;
    }
}
