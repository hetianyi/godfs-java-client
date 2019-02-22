package com.foxless.godfs.common;

public interface IDownloadReader {
    /**
     * pass the file info before download file.
     * if fileVO is null, it means file not exists.
     * @param fileVO
     */
    void before(FileVO fileVO);
    /**
     * custom handler for download file.
     * @param buffer
     * @param start
     * @param len
     */
    void read(byte[] buffer, int start, int len);

    /**
     * call when file download finish.
     * you must close your resource here.
     */
    void finish();
}
