package com.foxless.godfs.common;

public class Tracker {
    private String host;
    private Integer port;
    private String secret;

    // -------------getters and setters-------------

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
