package com.foxless.godfs.common;

import com.alibaba.fastjson.JSON;
import com.foxless.godfs.bean.EndPoint;
import com.foxless.godfs.bean.Member;
import com.foxless.godfs.bean.Meta;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.bean.meta.OperationGetStorageServerRequest;
import com.foxless.godfs.bean.meta.OperationGetStorageServerResponse;
import com.foxless.godfs.config.ClientConfigurationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

/**
 * TrackerMaintainer managed connection with tracker servers.
 * start in standalone thread in case of block main thread.
 */
public class TrackerMaintainer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TrackerMaintainer.class);

    private final IPool pool;
    private final ClientConfigurationBean configurationBean;

    public TrackerMaintainer(IPool pool, ClientConfigurationBean configurationBean) {
        this.pool = pool;
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
    }


    private class TimerSyncMemberTask extends TimerTask {

        private Tracker tracker;

        public TimerSyncMemberTask(Tracker tracker) {
            this.tracker = tracker;
        }

        @Override
        public void run() {
            EndPoint endPoint = EndPoint.fromTracker(tracker);
            pool.initEndPoint(endPoint, tracker.getMaxConnections());
            Bridge bridge = null;
            boolean broken = false;
            try {
                log.debug("synchronize storage servers.");
                Bridge _bridge = pool.getBridge(endPoint);
                bridge = _bridge;
                OperationGetStorageServerRequest validateMeta = new OperationGetStorageServerRequest();

                bridge.sendRequest(Const.O_SYNC_STORAGE, validateMeta, 0, null);
                bridge.receiveResponse(new IResponseHandler() {
                    @Override
                    public void handle(Meta meta, InputStream ips) throws Exception {
                        if (meta.getError() != null) {
                            throw meta.getError();
                        }
                        OperationGetStorageServerResponse response = JSON.parseObject(new String(meta.getMetaBody()), OperationGetStorageServerResponse.class);
                        log.debug("response status {} from server.", response.getStatus());
                        if (response.getStatus() == Const.STATUS_OK) {
                            if (null == response.getMembers()) {
                                return;
                            }
                            Set<Member> members = new HashSet<>(response.getMembers().length);
                            members.addAll(Arrays.asList(response.getMembers()));
                            MemberManager.refresh(tracker, members);
                        } else {
                            log.error("tracker server {}:{} response status err {}", _bridge.getConnection().getInetAddress().getHostAddress(), _bridge.getConnection().getPort(), response.getStatus());
                            throw new IllegalStateException("STATUS_BAD_SECRET");
                        }
                    }
                });

            } catch (Exception e) {
                broken = true;
                e.printStackTrace();
            } finally {
                if (broken) {
                    pool.returnBrokenBridge(endPoint, bridge);
                } else {
                    pool.returnBridge(endPoint, bridge);
                }
            }
        }
    }
}
