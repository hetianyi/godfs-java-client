package com.foxless.godfs.handler;

import com.alibaba.fastjson.JSON;
import com.foxless.godfs.bean.Meta;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.bean.meta.OperationQueryFileResponse;
import com.foxless.godfs.common.Bridge;
import com.foxless.godfs.common.Const;
import com.foxless.godfs.common.IReader;
import com.foxless.godfs.common.IResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryFileResponseHandler implements IResponseHandler {

    private static final Logger log = LoggerFactory.getLogger(QueryFileResponseHandler.class);

    @Override
    public Object handle(Bridge bridge, Tracker tracker, Meta meta, IReader byteReceiver) throws Exception {
        if (meta.getError() != null) {
            throw meta.getError();
        }
        OperationQueryFileResponse response = JSON.parseObject(new String(meta.getMetaBody()), OperationQueryFileResponse.class);
        log.debug("response status {} from server.", response.getStatus());
        if (response.getStatus() == Const.STATUS_OK || response.getStatus() == Const.STATUS_NOT_FOUND) {
            if (!response.isExist() || null == response.getFileEntity()) {
                return null;
            }
            return response.getFileEntity();
        } else {
            log.error("tracker server {}:{} response err status {}", bridge.getConnection().getInetAddress().getHostAddress(), bridge.getConnection().getPort(), response.getStatus());
            throw new IllegalStateException("tracker server response error status: " + response.getStatus());
        }
    }
}
