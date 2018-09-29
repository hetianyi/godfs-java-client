package com.foxless.godfs.api;

import com.foxless.godfs.bean.FileEntity;
import com.foxless.godfs.bean.MonitorProgressBean;
import com.foxless.godfs.common.IMonitor;
import com.foxless.godfs.common.IReader;

import java.io.File;
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

    /**
     * upload file in stream mode.
     * @param ips
     * @param fileSize
     * @param group
     * @param monitor
     * @return
     * @throws Exception
     */
    String upload(InputStream ips, long fileSize, String group, IMonitor<MonitorProgressBean> monitor) throws Exception;

    /**
     * upload local file.
     * @param file
     * @param group
     * @param monitor
     * @return
     * @throws Exception
     */
    String upload(File file, String group, IMonitor<MonitorProgressBean> monitor) throws Exception;

    /**
     * download file using file path which pattern like 'G01/001/S/&lt;md5&gt;'
     *
     * @param path file path which pattern like 'G01/001/S/&lt;md5&gt;'
     * @param byteReceiver byte receiver handling download file bytes.
     * @throws Exception
     */
    void download(String path, long start, long offset, IReader byteReceiver) throws Exception;
}
