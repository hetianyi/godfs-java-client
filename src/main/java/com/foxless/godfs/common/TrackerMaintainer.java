package com.foxless.godfs.common;

import com.foxless.godfs.GoDFSClient;
import com.foxless.godfs.bean.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TrackerMaintainer {

    private static final Logger log = LoggerFactory.getLogger(TrackerMaintainer.class);

    private List<TrackerInstance> trackers;

    public void maintain(List<Tracker> trackers) {
        trackers = new ArrayList<>(trackers.size());
        for (Tracker tracker : trackers) {
            tracks(tracker);
        }
    }

    private void tracks(Tracker tracker) {
        TrackerInstance instance = new TrackerInstance(tracker);
        trackers.add(instance);
        new Thread(instance).start();
    }
}
