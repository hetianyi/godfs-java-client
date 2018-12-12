package com.foxless.godfs.bean.meta;

public class OperationValidationResponse {
    private Integer status;
    private String uuid;
    private Boolean isnew;// tracker是否标志新client

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

    public Boolean isIsnew() {
        return isnew;
    }

    public void setIsnew(Boolean isnew) {
        this.isnew = isnew;
    }
}
