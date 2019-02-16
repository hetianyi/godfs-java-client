package com.foxless.godfs.common;

public class FileVO {
    private Long id;
    private String md5;
    private Integer parts_num;
    private String group;
    private String instance;
    private Integer finish;
    private Long file_size;
    private PartDO[] parts;
}
