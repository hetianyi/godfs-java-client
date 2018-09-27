package com.foxless.godfs.api.impl;

import com.foxless.godfs.api.GodfsApiClient;
import com.foxless.godfs.bean.EndPoint;
import com.foxless.godfs.bean.File;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.bean.meta.OperationQueryFileRequest;
import com.foxless.godfs.common.Bridge;
import com.foxless.godfs.common.Const;
import com.foxless.godfs.config.ClientConfigurationBean;
import com.foxless.godfs.handler.QueryFileResponseHandler;
import com.foxless.godfs.handler.SyncStorageResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * godfs api client implementation.
 * for file upload, file download, file query etc.
 *
 * @author hehety
 * @sine 1.0
 * @date 2018/09/26
 * @version 1.0
 */
public class GodfsApiClientImpl implements GodfsApiClient {

    private static final Logger log = LoggerFactory.getLogger(GodfsApiClientImpl.class);

    private static GodfsApiClientImpl godfsApiClientInstance;

    private final ClientConfigurationBean configuration;

    private GodfsApiClientImpl(ClientConfigurationBean configuration) {
        this.configuration = configuration;
    }

    public static synchronized final GodfsApiClient getInstance(ClientConfigurationBean configuration) {
        if (null == godfsApiClientInstance) {
            godfsApiClientInstance = new GodfsApiClientImpl(configuration);
        }
        return godfsApiClientInstance;
    }

    @Override
    public File query(String pathOrMd5) throws Exception {
        if (null == pathOrMd5 || "".equals(pathOrMd5)) {
            log.warn("query parameter cannot be null or empty");
            return null;
        }
        pathOrMd5 = pathOrMd5.trim();
        if (!pathOrMd5.matches(Const.PATH_REGEX) && !pathOrMd5.matches(Const.MD5_REGEX)) {
            log.warn("query parameter mismatch pattern");
            return null;
        }
        if (pathOrMd5.indexOf("/") != -1) {
            pathOrMd5 = "/" + pathOrMd5;
        }

        if (null != configuration.getTrackers()) {
            for (Tracker tracker : configuration.getTrackers()) {
                EndPoint endPoint = EndPoint.fromTracker(tracker);
                Const.getPool().initEndPoint(endPoint, tracker.getMaxConnections());
                Bridge bridge = null;
                boolean broken = false;
                try {
                    log.debug("query file '{}' from tracker server: {}:{}", pathOrMd5, tracker.getHost(), tracker.getPort());
                    bridge = Const.getPool().getBridge(endPoint);
                    OperationQueryFileRequest queryFileRequest = new OperationQueryFileRequest();
                    queryFileRequest.setMd5(pathOrMd5);
                    bridge.sendRequest(Const.O_QUERY_FILE, queryFileRequest, 0, null);
                    File file = (File) bridge.receiveResponse(tracker, QueryFileResponseHandler.class);
                    if (null != file) {
                        return file;
                    }
                    continue;
                } catch (Exception e) {
                    broken = true;
                    log.error("error query file from tracker server [{}:{}] due to: {}", tracker.getHost(), tracker.getPort(), e.getMessage());
                    throw e;
                } finally {
                    if (broken) {
                        Const.getPool().returnBrokenBridge(endPoint, bridge);
                    } else {
                        Const.getPool().returnBridge(endPoint, bridge);
                    }
                }
            }
        } else {
            return null;
        }
        return null;
    }

    public final ClientConfigurationBean getConfiguration() {
        return configuration;
    }
}
