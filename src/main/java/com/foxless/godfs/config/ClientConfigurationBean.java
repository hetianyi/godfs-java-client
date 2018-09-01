package com.foxless.godfs.config;

import com.foxless.godfs.bean.Tracker;

import java.io.Serializable;
import java.util.List;

/**
 * 客户端配置bean
 *
 * @author hetianyi
 * @version 0.1.0
 * @date 2018/09/02
 */
public class ClientConfigurationBean implements Serializable {

    private static final long serialVersionUID = 1793671443182588930L;
    /**
     * tracker服务器列表
     */
    private List<Tracker> trackers;

    public List<Tracker> getTrackers() {
        return trackers;
    }

    public void setTrackers(List<Tracker> trackers) {
        this.trackers = trackers;
    }
}
