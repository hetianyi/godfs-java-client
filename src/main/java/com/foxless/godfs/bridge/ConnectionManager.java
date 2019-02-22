package com.foxless.godfs.bridge;

import com.foxless.godfs.common.ServerInfo;

import java.io.IOException;
import java.net.Socket;

public class ConnectionManager {

    // storage server info
    private ServerInfo server;
    private Socket conn;
    // represent this connection is server side(1) or client side(2)
    private Integer side;
    // connect state
    // 0: not connect
    // 1: connected but not validate
    // 2: validated
    // 3: disconnected
    private Integer state;
    // storage uuid, this field is used by server side.
    private String uuid;

    public ConnectionManager() {
    }

    public ConnectionManager(ServerInfo server, Socket conn, Integer side) {
        this.server = server;
        this.conn = conn;
        this.side = side;
    }

    // Close close manager and return connection to pool.
    public void close() {
        if (this.conn != null) {
            Commons.connPool.returnConnBridge(this.server, this.conn);
        }
    }

    // Destroy close manager and close connection.
    public void destroy() {
        if (this.server == null) {
            if (this.conn != null) {
                try {
                    this.conn.close();
                } catch (IOException e) {}
            }
            return;
        }
        if (this.conn != null) {
            Commons.connPool.returnBrokenConnBridge(this.server, this.conn);
        }
    }

    // Receive receive data frame from server/client
    public Frame receive() throws IOException {
        return StreamResolver.readFrame(this);
    }

    // Send send data to from server/client
    public void send(Frame frame) throws IOException {
        StreamResolver.writeFrame(this, frame);
    }

    // RequireStatus assert status.
    public void requireStatus(int requiredState) {
        if (this.state < requiredState) {
            throw new IllegalStateException("connect state not satisfied, expect "
                    + requiredState + ", now is " + this.state);
        }
    }



    // -------------getters and setters-------------

    public ServerInfo getServer() {
        return server;
    }

    public void setServer(ServerInfo server) {
        this.server = server;
    }

    public Socket getConn() {
        return conn;
    }

    public void setConn(Socket conn) {
        this.conn = conn;
    }

    public Integer getSide() {
        return side;
    }

    public void setSide(Integer side) {
        this.side = side;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
