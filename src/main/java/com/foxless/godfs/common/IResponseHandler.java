package com.foxless.godfs.common;

import com.foxless.godfs.bean.Meta;

import java.io.InputStream;

public interface IResponseHandler {
    void handle(Meta meta, InputStream ips) throws Exception;
}
