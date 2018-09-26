package com.foxless.godfs.api;

import com.foxless.godfs.bean.File;

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
    File query(String pathOrMd5) throws Exception;
}
