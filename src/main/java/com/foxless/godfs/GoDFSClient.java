package com.foxless.godfs;

import com.alibaba.fastjson.JSON;
import com.foxless.godfs.api.GodfsApiClient;
import com.foxless.godfs.api.impl.GodfsApiClientImpl;
import com.foxless.godfs.common.ConnectionPool;
import com.foxless.godfs.common.Const;
import com.foxless.godfs.common.IPool;
import com.foxless.godfs.common.TrackerMaintainer;
import com.foxless.godfs.config.ClientConfigurationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * godfs java client entry class.
 * TODO print tracker servers info on boot.
 * @author hehety
 * @sine 1.0
 * @date 2018/09/26
 * @version 1.0
 */
public class GoDFSClient {

    private static final Logger log = LoggerFactory.getLogger(GoDFSClient.class);

    private ClientConfigurationBean configuration;

    private boolean init = false;

    public GoDFSClient(ClientConfigurationBean configuration) {
        if (null == configuration) {
            throw new IllegalArgumentException("configuration cannot be null.");
        }
        log.debug("init client with config: {}", JSON.toJSONString(configuration));
        this.configuration = configuration;
    }

    public void start() {
        log.debug("starting godfs client");
        Const.initPool(configuration);
        TrackerMaintainer maintainer = new TrackerMaintainer(configuration);
        new Thread(maintainer).start();
        init = true;
    }


    public GodfsApiClient getGodfsApiClient() {
        if (!init) {
            throw new IllegalStateException("godfs client is not ready, please initial it first!");
        }
        return GodfsApiClientImpl.getInstance(configuration);
    }

}
