package com.foxless.godfs.common;

import com.foxless.godfs.bean.ExpireMember;
import com.foxless.godfs.bean.Member;
import com.foxless.godfs.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientConnectionPool implements IPool {

    private static final Logger log = LoggerFactory.getLogger(ClientConnectionPool.class);

    private Map<String, List<Bridge>> connMap;
    private Map<String, Integer> activeConnCounter;
    private int maxConnPerServer;
    private int totalActiveConn;

    public synchronized void init(int maxConnPerServer) {
        this.connMap = new HashMap<String, List<Bridge>>();
        this.activeConnCounter = new HashMap<String, Integer>();
        if (maxConnPerServer <= 0 || maxConnPerServer > 100) {
            maxConnPerServer = 10;
        }
        this.maxConnPerServer = maxConnPerServer;
    }

    public Bridge getConnBridge(ExpireMember member) {
        List<Bridge> list = getConnMap(member);
        if (list.size() > 0) {
            return list.remove(0);
        }
        if (this.increaseActiveConnection(member, 0) < this.maxConnPerServer) {
            return this.newConnection(server)
        }
        return null;
    }


    public Bridge newConnection(ExpireMember server) throws Exception {
        log.debug("connecting to storage server...");
        Socket con = new Socket(server.getAddr(), server.getPort());
        Bridge connBridge = new Bridge(con);

        isNew, e1 := connBridge.ValidateConnection(app.SECRET)
        if e1 != nil {
            connBridge.Close()
            return nil, e1
        }

        // if the client is new to tracker server, then update the client master_sync_id from 0.
        if isNew && app.CLIENT_TYPE == 1  {
            logger.Info("I'm new to tracker:", connBridge.GetConn().RemoteAddr().String(), "[", connBridge.UUID, "]")
            e2 := lib_service.UpdateTrackerSyncId(connBridge.UUID, 0, nil)
            if e2 != nil {
                connBridge.Close()
                return nil, e2
            }
        }
        logger.Debug("successful validate connection:", e1)
        pool.IncreaseActiveConnection(server, 1)
        return connBridge, nil
    }

    public void returnConnBridge(Member member, Bridge bridge) {

    }

    public void increaseActiveConnection(Member member, int value) {

    }

    private List<Bridge> getConnMap(ExpireMember server) {
        String uid =Utils.getStorageServerUID(server);
        List<Bridge> connList = this.connMap.get(uid);
        if (connList == null) {
            connList = new ArrayList<Bridge>(maxConnPerServer);
        }
        this.connMap.put(uid, connList);
        return connList;
    }
}
