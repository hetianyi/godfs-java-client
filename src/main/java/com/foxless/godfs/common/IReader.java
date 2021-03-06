package com.foxless.godfs.common;

public interface IReader {
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
