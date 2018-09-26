package com.foxless.godfs.bean;

import java.util.List;

/**
 * file
 *
 * @author hehety
 * @sine 1.0
 * @date 2018/09/26
 * @version 1.0
 */
public class File {

    private long id;
    private String md5;
    private int partNum;
    private String group;
    private String instance;
    private List<FilePart> parts;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getPartNum() {
        return partNum;
    }

    public void setPartNum(int partNum) {
        this.partNum = partNum;
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

    public List<FilePart> getParts() {
        return parts;
    }

    public void setParts(List<FilePart> parts) {
        this.parts = parts;
    }
}
