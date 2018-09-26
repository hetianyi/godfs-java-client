package com.foxless.godfs.config;

import com.foxless.godfs.bean.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户端配置bean
 *
 * @author hetianyi
 * @version 0.1.0
 * @date 2018/09/02
 * @version 1.0
 */
public class ClientConfigurationBean implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(ClientConfigurationBean.class);

    private static final long serialVersionUID = 1793671443182588930L;

    /**
     * tracker服务器列表
     */
    private List<Tracker> trackers;

    private String secret = "";

    public synchronized void addTracker(Tracker tracker) {
        if (null == tracker) {
            log.error("cannot add a null tracker!");
        }
        if (null == trackers) {
            log.debug("init tracker list.");
            trackers = new ArrayList<Tracker>(2);
        }
        trackers.add(tracker);
    }

    public List<Tracker> getTrackers() {
        return trackers;
    }

    public void setTrackers(List<Tracker> trackers) {
        this.trackers = trackers;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
