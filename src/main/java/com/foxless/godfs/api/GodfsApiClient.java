package com.foxless.godfs.api;


import com.foxless.godfs.common.*;

import javax.servlet.http.HttpServletRequest;
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
    FileVO query(String pathOrMd5) throws Exception;

    /**
     * upload file in stream mode.
     * @param ips
     * @param fileSize
     * @param group the group of the file
     * @param monitor upload progress monitor
     * @return file id like "G01/001/90234afcbba2314123112390234afcbb"
     * @throws Exception
     */
    String upload(InputStream ips, long fileSize, String group, IMonitor<MonitorProgressBean> monitor) throws Exception;
    /**
     * upload file in stream mode.
     * @param ips
     * @param fileSize
     * @return file id like "G01/001/90234afcbba2314123112390234afcbb"
     * @throws Exception
     */
    String upload(InputStream ips, long fileSize) throws Exception;
    /**
     * upload local file.
     * @param file the file to be uploaded
     * @param group the group of the file
     * @param monitor upload progress monitor
     * @return file id like "G01/001/90234afcbba2314123112390234afcbb"
     * @throws Exception
     */
    String upload(File file, String group, IMonitor<MonitorProgressBean> monitor) throws Exception;
    /**
     * upload local file.
     * @param file the file to be uploaded
     * @return file id like "G01/001/90234afcbba2314123112390234afcbb"
     * @throws Exception
     */
    String upload(File file) throws Exception;
    /**
     * streaming api for uploading file.<br/>
     * NOTE: <i>this way needs storage server enable http upload.</i>
     * @param request the http request, if you are using spring ---------------------------
     * @param group the group of the file
     * @param monitor upload progress monitor
     * @param protocol only http or https, default is http.
     * @return
     * @throws Exception
     */
    String upload(HttpServletRequest request, String group, IMonitor<MonitorProgressBean> monitor, String protocol) throws Exception;
    /**
     * streaming api for uploading file.<br/>
     * NOTE: <i>this way needs storage server enable http upload.</i>
     * @param request the http request, if you are using spring ---------------------------
     * @return
     * @throws Exception
     */
    String upload(HttpServletRequest request) throws Exception;

    /**
     * download file using file path which pattern like 'G01/001/S/&lt;md5&gt;'
     *
     * @param path file path which pattern like 'G01/001/S/&lt;md5&gt;'
     * @param start download file start position.
     * @param offset download bytes offset, -1 represents the end of the file.
     * @param byteReceiver byte receiver handling download file bytes.
     * @throws Exception
     */
    void download(String path, long start, long offset, IReader byteReceiver) throws Exception;

    /**
     * @see #download(String, long, long, IReader)
     */
    void download(String path, IReader byteReceiver) throws Exception;
}
