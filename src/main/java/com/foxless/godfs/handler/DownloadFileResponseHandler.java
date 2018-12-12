package com.foxless.godfs.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxless.godfs.bean.Meta;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.bean.meta.OperationDownloadFileResponse;
import com.foxless.godfs.common.Bridge;
import com.foxless.godfs.common.Const;
import com.foxless.godfs.common.IReader;
import com.foxless.godfs.common.IResponseHandler;
import com.foxless.godfs.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class DownloadFileResponseHandler implements IResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(DownloadFileResponseHandler.class);
    @Override
    public Object handle(Bridge bridge, Tracker tracker, Meta meta, IReader byteReceiver) throws Exception {
        if (meta.getError() != null) {
            throw meta.getError();
        }
        ObjectMapper objectMapper = Utils.getObjectMapper();
        OperationDownloadFileResponse response = objectMapper.readValue(new String(meta.getMetaBody()), OperationDownloadFileResponse.class);
        log.debug("response status {} from server.", response.getStatus());
        if (response.getStatus() == Const.STATUS_NOT_FOUND) {
            throw new FileNotFoundException("file not found");
        } else if (response.getStatus() == Const.STATUS_OK) {
            InputStream ips = bridge.getConnection().getInputStream();
            long bodyLength = meta.getBodyLength();
            byte[] buffer = new byte[Const.BUFFER_SIZE];
            int len;
            long finish = 0l;
            long left = bodyLength;
            int nextRead = Const.BUFFER_SIZE;
            if (bodyLength < Const.BUFFER_SIZE) {
                nextRead = (int) left;
                while(left > 0 && (len = ips.read(buffer, 0, nextRead)) != -1) {
                    if (null != byteReceiver) {
                        byteReceiver.read(buffer, 0, len);
                    }
                    finish += len;
                    left = bodyLength - finish;
                    nextRead = (int) left;
                }
            } else {
                while(left > 0 && (len = ips.read(buffer, 0, nextRead)) != -1) {
                    if (null != byteReceiver) {
                        byteReceiver.read(buffer, 0, len);
                    }
                    finish += len;
                    left = bodyLength - finish;
                    if (left < Const.BUFFER_SIZE) {
                        nextRead = (int) left;
                    }
                }
            }

            if (null != byteReceiver) {
                byteReceiver.finish();
            }
            return null;
        } else {
            log.error("storage server {}:{} response err status {}", bridge.getConnection().getInetAddress().getHostAddress(), bridge.getConnection().getPort(), response.getStatus());
            throw new IllegalStateException("storage server response error status: " + response.getStatus());
        }
    }
}
