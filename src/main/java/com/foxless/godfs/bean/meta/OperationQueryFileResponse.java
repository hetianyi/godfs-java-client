package com.foxless.godfs.bean.meta;

import com.foxless.godfs.bean.FileEntity;

public class OperationQueryFileResponse {
    private int status;
    private boolean exist;
    private FileEntity fileEntity;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public FileEntity getFileEntity() {
        return fileEntity;
    }

    public void setFileEntity(FileEntity fileEntity) {
        this.fileEntity = fileEntity;
    }
}
