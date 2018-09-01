package com.foxless.godfs.bean;

/**
 * tracker服务实例
 *
 * @author hetianyi
 * @version 0.1.0
 * @date 2018/09/02
 */
public class Tracker {

    private String host;

    private int port = 1022;

    private boolean online;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
