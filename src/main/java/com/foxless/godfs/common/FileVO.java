package com.foxless.godfs.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileVO {
    private Long id;
    private String md5;
    @JsonProperty("parts_num")
    private int partsNumber;
    private String group;
    private String instance;
    private int finish;
    @JsonProperty("file_size")
    private long fileSize;
    private PartDO[] parts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getPartsNumber() {
        return partsNumber;
    }

    public void setPartsNumber(int partsNumber) {
        this.partsNumber = partsNumber;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public int getFinish() {
        return finish;
    }

    public void setFinish(int finish) {
        this.finish = finish;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public PartDO[] getParts() {
        return parts;
    }

    public void setParts(PartDO[] parts) {
        this.parts = parts;
    }
}
