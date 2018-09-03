package com.foxless.godfs.common;

import com.foxless.godfs.bean.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackerInstance implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(TrackerInstance.class);

    private Tracker tracker;
    public TrackerInstance(Tracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void run() {
        log.debug("start tracker conn with tracker server: {}:{}", tracker.getHost(), tracker.getPort());
        int retry = 0;
    }
}
