package com.foxless.godfs.bean.meta;

import com.foxless.godfs.bean.File;

public class OperationQueryFileResponse {
    private int status;
    private boolean exist;
    private File file;

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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
