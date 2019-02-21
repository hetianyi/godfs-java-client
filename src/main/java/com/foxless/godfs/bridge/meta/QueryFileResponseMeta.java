package com.foxless.godfs.bridge.meta;

import com.foxless.godfs.common.FileVO;

public class QueryFileResponseMeta {
    private boolean exist;
    private FileVO file;

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public FileVO getFile() {
        return file;
    }

    public void setFile(FileVO file) {
        this.file = file;
    }
}
