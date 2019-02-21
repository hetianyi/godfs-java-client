package com.foxless.godfs;

import com.foxless.godfs.common.Tracker;
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

    /**
     * tracker服务器列表
     */
    private List<Tracker> trackers;
    private Integer maxConnections;

    public synchronized void addTracker(Tracker tracker) {
        if (null == tracker) {
            log.error("cannot add a null tracker!");
        }
        if (null == trackers) {
            log.debug("init tracker list.");
            trackers = new ArrayList<>(2);
        }
        trackers.add(tracker);
    }

    public List<Tracker> getTrackers() {
        return trackers;
    }

    public void setTrackers(List<Tracker> trackers) {
        this.trackers = trackers;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }
}
