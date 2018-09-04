package com.foxless.godfs.bean.meta;

public class OperationValidationResponse {
    private Integer status;
    private String uuid;
    private boolean isnew;// tracker是否标志新client

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isIsnew() {
        return isnew;
    }

    public void setIsnew(boolean isnew) {
        this.isnew = isnew;
    }
}
