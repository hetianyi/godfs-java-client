package com.foxless.godfs.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxless.godfs.bridge.meta.*;
import com.foxless.godfs.common.AddrTuple;
import com.foxless.godfs.common.ObjectTuple;
import com.foxless.godfs.common.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class TcpBridgeClient {

    private static final Logger logger = LoggerFactory.getLogger(TcpBridgeClient.class);

    private ServerInfo server;
    private ConnectionManager connManager;

    public TcpBridgeClient(ServerInfo server) {
        if (null == server) {
            throw new IllegalArgumentException("server cannot be null");
        }
        this.server = server;
    }

    public ConnectionManager getConnManager() {
        return this.connManager;
    }

    // Connect connect to server
    public void connect() throws Exception {
        if (this.connManager != null && this.connManager.getState() > Commons.STATE_NOT_CONNECT) {
            throw new IllegalStateException("already connected");
        }
        Socket conn = Commons.connPool.getConn(this.server);
        AddrTuple add = this.server.GetHostAndPortByAccessFlag();
        if (null == conn) {
            throw new IOException("error get connection from server " + add.getHost() + ":" + add.getPort());
        }
        logger.debug("connect to {}:{} success", add.getHost(), add.getPort());
        this.connManager = new ConnectionManager(this.server, conn, Commons.CLIENT_SIDE);
        this.connManager.setState(Commons.STATE_CONNECTED);
    }

    // Validate validate this connection.
    public ConnectResponseMeta validate() throws IOException {
        ConnectMeta meta = new ConnectMeta(server.getSecret(), Commons.UUID);
        Frame frame = this.sendReceive(Commons.FRAME_OPERATION_VALIDATE, Commons.STATE_CONNECTED, meta, 0, null);

        ConnectResponseMeta res = parseResponseMeta(frame, ConnectResponseMeta.class);
        if (frame.getStatus() == Commons.STATUS_SUCCESS) {
            this.connManager.setState(Commons.STATE_VALIDATED);
            this.connManager.setUuid(res.getUuid());
        } else {
            throw new IllegalStateException("error validate with tracker server " + server.getHost() + ":" + server.getPort());
        }
        return res;
    }

    // SyncAllStorageServers synchronized storage members.
    public SyncAllStorageServerResponseMeta syncAllStorageServers(SyncAllStorageServerMeta meta) throws IOException {
        Frame frame = this.sendReceive(Commons.FRAME_OPERATION_SYNC_ALL_STORAGE_SERVERS, Commons.STATE_VALIDATED, meta, 0, null);
        return parseResponseMeta(frame, SyncAllStorageServerResponseMeta.class);
    }


    // UploadFile upload file to storage server.
    public UploadFileResponseMeta uploadFile(UploadFileMeta meta, IHandler bodyWriterHandler) throws IOException {
        Frame frame = this.sendReceive(Commons.FRAME_OPERATION_UPLOAD_FILE, Commons.STATE_VALIDATED, meta, meta.getFileSize(), bodyWriterHandler);
        return parseResponseMeta(frame, UploadFileResponseMeta.class);
    }

    // QueryFile pull files from tracker
    public QueryFileResponseMeta queryFile(QueryFileMeta meta) throws IOException {
        Frame frame = this.sendReceive(Commons.FRAME_OPERATION_QUERY_FILES, Commons.STATE_VALIDATED, meta, 0, null);
        String metaStr = new String(frame.getFrameMeta());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(metaStr, QueryFileResponseMeta.class);
    }

    // DownloadFile download file from storage server.
    public ObjectTuple<DownloadFileResponseMeta, Frame> downloadFile(DownloadFileMeta meta) throws IOException {
        Frame frame = this.sendReceive(Commons.FRAME_OPERATION_DOWNLOAD_FILE, Commons.STATE_VALIDATED, meta, 0, null);
        DownloadFileResponseMeta res = parseResponseMeta(frame, DownloadFileResponseMeta.class);
        return new ObjectTuple(res, frame);
    }

    // sendReceive send request and receive response,
    // returns response frame and error.
    public Frame sendReceive(byte operation,
                             int statusRequire,
                             Object meta,
                             long bodyLength,
                             IHandler bodyWriterHandler) throws IOException {

        try {
            this.connManager.requireStatus(statusRequire);
            Frame frame = new Frame();
            frame.setOperation(operation);
            frame.setMeta(meta);
            frame.setMetaBodyLength(bodyLength);
            frame.setBodyWriterHandler(bodyWriterHandler);
            this.connManager.send(frame);

            Frame response = this.connManager.receive();
            if (response != null) {
                return response;
            } else {
                throw new IllegalStateException("receive empty response from server");
            }
        } catch (Exception e) {
            Commons.connPool.returnBrokenConnBridge(server, null == this.connManager ? null : this.connManager.getConn());
            throw e;
        }
    }


    public <T> T parseResponseMeta(Frame frame, Class<T> type) throws IOException {
        String metaStr = new String(frame.getFrameMeta());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(metaStr, type);
    }

    public void close() {
        if (null != this.getConnManager()) {
            this.getConnManager().close();
        }
    }

    public void destory() {
        if (null != this.getConnManager()) {
            this.getConnManager().destroy();
        }
    }
}
