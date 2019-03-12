package com.foxless.godfs.client;

import com.foxless.godfs.ClientConfigurationBean;
import com.foxless.godfs.bridge.Commons;
import com.foxless.godfs.bridge.TcpBridgeClient;
import com.foxless.godfs.bridge.meta.ConnectResponseMeta;
import com.foxless.godfs.bridge.meta.SyncAllStorageServerMeta;
import com.foxless.godfs.bridge.meta.SyncAllStorageServerResponseMeta;
import com.foxless.godfs.common.ServerInfo;
import com.foxless.godfs.common.StorageDO;
import com.foxless.godfs.common.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * TrackerMaintainer managed connection with tracker servers.
 * start in standalone thread in case of block main thread.
 *
 * @author hehety
 * @version 1.0
 * @sine 1.0
 * @date 2018/09/26
 */
public class TrackerMaintainer {

    private static final Logger logger = LoggerFactory.getLogger(TrackerMaintainer.class);

    private final ClientConfigurationBean configurationBean;

    public static final int SCHEDULE_INTERVAL = 30000;

    public TrackerMaintainer(ClientConfigurationBean configurationBean) {
        this.configurationBean = configurationBean;
        init();
    }

    private void init() {
        if (null != configurationBean.getTrackers()) {
            for (Tracker tracker : configurationBean.getTrackers()) {
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerSyncMemberTask(tracker), 0, SCHEDULE_INTERVAL);
            }
        }
        // start storage member expire work
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                MemberManager.expireMember();
            }
        }, SCHEDULE_INTERVAL * 3 + 5000, SCHEDULE_INTERVAL * 3);
    }


    private class TimerSyncMemberTask extends TimerTask {
        private Tracker tracker;
        private TcpBridgeClient client;

        public TimerSyncMemberTask(Tracker tracker) {
            this.tracker = tracker;
        }

        @Override
        public void run() {
            try {
                logger.debug("synchronize storage servers.");
                if (null == client || client.getConnManager() == null) {
                    ServerInfo server = ServerInfo.fromConnStr(tracker.getHost() + ":" + tracker.getPort());
                    server.setSecret(tracker.getSecret());
                    client = new TcpBridgeClient(server);
                    client.connect();
                    client.validate();
                }
                SyncAllStorageServerResponseMeta respMeta = client.syncAllStorageServers(new SyncAllStorageServerMeta());
                if (respMeta.getServers() != null && respMeta.getServers().length > 0) {
                    logger.debug("find storage servers: {}", respMeta.getServers().length);
                    Set<StorageDO> servers = new HashSet<StorageDO>(respMeta.getServers().length);
                    for (StorageDO s : respMeta.getServers()) {
                        servers.add(s);
                    }
                    MemberManager.refresh(tracker, servers);
                } else {
                    logger.debug("no storage server found.");
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
                if (null != client) {
                    client.destroy();
                }
            } finally {
                if (null != client) {
                    client.close();
                }
            }
        }
    }
}
