package com.foxless.godfs.common;

import com.foxless.godfs.bean.Meta;
import com.foxless.godfs.bean.Tracker;

import java.io.InputStream;

public interface IResponseHandler {
    Object handle(Bridge bridge, Tracker tracker, Meta meta, InputStream ips) throws Exception;
}
