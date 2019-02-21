package com.foxless.godfs.bridge.meta;

// ConnectMeta operation meta for connect/validate
public class ConnectMeta {
    private String secret;
    private String uuid;

    public ConnectMeta(String secret, String uuid) {
        this.secret = secret;
        this.uuid = uuid;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
