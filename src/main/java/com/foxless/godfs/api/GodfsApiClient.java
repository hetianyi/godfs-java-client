package com.foxless.godfs.api;

import com.foxless.godfs.bean.FileEntity;
import com.foxless.godfs.bean.MonitorProgressBean;
import com.foxless.godfs.common.IMonitor;

import java.io.InputStream;

/**
 * godfs api client.
 * for file upload, file download, file query etc.
 *
 * @author hehety
 * @sine 1.0
 * @date 2018/09/26
 * @version 1.0
 */
public interface GodfsApiClient {
    /**
     * query file info from tracker server.
     * @param pathOrMd5 path like "G01/001/90234afcbba2314123112390234afcbb" or just a file md5
     * @throws Exception
     */
    FileEntity query(String pathOrMd5) throws Exception;

    String upload(InputStream ips, long fileSize, String group, IMonitor<MonitorProgressBean> monitor) throws Exception;
}
