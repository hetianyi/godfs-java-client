package com.foxless.godfs.bean;

import java.net.Socket;

public class Bridge {
    private Socket connection;
    private String uuid;

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
}
