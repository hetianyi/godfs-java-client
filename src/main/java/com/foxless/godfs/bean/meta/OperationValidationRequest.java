package com.foxless.godfs.bean.meta;

public class OperationValidationRequest {
    private String secret;
    private String  uuid;

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
