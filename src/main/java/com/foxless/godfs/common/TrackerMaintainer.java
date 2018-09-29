package com.foxless.godfs.common;

import com.foxless.godfs.bean.EndPoint;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.bean.meta.OperationGetStorageServerRequest;
import com.foxless.godfs.config.ClientConfigurationBean;
import com.foxless.godfs.handler.SyncStorageResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TrackerMaintainer managed connection with tracker servers.
 * start in standalone thread in case of block main thread.
 *
 * @author hehety
 * @sine 1.0
 * @date 2018/09/26
 * @version 1.0
 */
public class TrackerMaintainer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TrackerMaintainer.class);

    private final ClientConfigurationBean configurationBean;

    public TrackerMaintainer(ClientConfigurationBean configurationBean) {
        this.configurationBean = configurationBean;
    }

    @Override
    public void run() {
        if (null != configurationBean.getTrackers()) {
            for (Tracker tracker : configurationBean.getTrackers()) {
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerSyncMemberTask(tracker), 0, 30000);
            }
        }
        // start storage member expire work
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                MemberManager.expireMember();
            }
        }, 30000*3 + 5000, 30000*3);
    }


    private class TimerSyncMemberTask extends TimerTask {

        private Tracker tracker;

        public TimerSyncMemberTask(Tracker tracker) {
            this.tracker = tracker;
        }

        @Override
        public void run() {
            EndPoint endPoint = EndPoint.fromTracker(tracker);
            Const.getPool().initEndPoint(endPoint, tracker.getMaxConnections());
            Bridge bridge = null;
            boolean broken = false;
            try {
                log.debug("synchronize storage servers.");
                Bridge _bridge = Const.getPool().getBridge(endPoint);
                bridge = _bridge;
                OperationGetStorageServerRequest validateMeta = new OperationGetStorageServerRequest();

                bridge.sendRequest(Const.O_SYNC_STORAGE, validateMeta, 0, null);
                bridge.receiveResponse(tracker, SyncStorageResponseHandler.class, null);
            } catch (Exception e) {
                broken = true;
                e.printStackTrace();
            } finally {
                if (broken) {
                    Const.getPool().returnBrokenBridge(endPoint, bridge);
                } else {
                    Const.getPool().returnBridge(endPoint, bridge);
                }
            }
        }
    }
}
