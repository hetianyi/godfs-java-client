package com.foxless.godfs.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxless.godfs.bean.Meta;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.bean.meta.OperationUploadFileResponse;
import com.foxless.godfs.common.Bridge;
import com.foxless.godfs.common.Const;
import com.foxless.godfs.common.IReader;
import com.foxless.godfs.common.IResponseHandler;
import com.foxless.godfs.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadResponseHandler implements IResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(UploadResponseHandler.class);

    @Override
    public Object handle(Bridge bridge, Tracker tracker, Meta meta, IReader byteReceiver) throws Exception {
        if (meta.getError() != null) {
            throw meta.getError();
        }
        ObjectMapper objectMapper = Utils.getObjectMapper();
        OperationUploadFileResponse response = objectMapper.readValue(new String(meta.getMetaBody()), OperationUploadFileResponse.class);
        log.debug("response status {} from server.", response.getStatus());
        if (response.getStatus() == Const.STATUS_OK) {
            return response.getPath();
        } else {
            log.error("tracker server {}:{} response err status {}", bridge.getConnection().getInetAddress().getHostAddress(), bridge.getConnection().getPort(), response.getStatus());
            throw new IllegalStateException("STATUS_BAD_SECRET");
        }
    }
}
