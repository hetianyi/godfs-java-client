package com.foxless.godfs;

import com.alibaba.fastjson.JSON;
import com.foxless.godfs.config.ClientConfigurationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoDFSClient {

    private static final Logger log = LoggerFactory.getLogger(GoDFSClient.class);

    private ClientConfigurationBean configuration;

    public GoDFSClient(ClientConfigurationBean configuration) {
        if (null == configuration) {
            throw new IllegalArgumentException("configuration cannot be null.");
        }
        log.debug("init client with config: {}", JSON.toJSONString(configuration));
        this.configuration = configuration;
    }

    public void start() {
        log.debug("start client");

    }


}
