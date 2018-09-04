package com.foxless.godfs.common;

import com.foxless.godfs.bean.EndPoint;
import com.foxless.godfs.config.ClientConfigurationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

// TODO expire connection
public class ConnectionPool implements IPool {
    private static final Logger log = LoggerFactory.getLogger(ConnectionPool.class);

    // connections for each endpoint.
    private final Map<String, LinkedList<Bridge>> conns = new HashMap<String, LinkedList<Bridge>>();
    // log max connections for each endpoint.
    private final Map<String, Integer> connConfig = new HashMap<String, Integer>();
    // log current connections for each endpoint.
    private final Map<String, Integer> currentConns = new HashMap<String, Integer>();
    // prepared lock for each endpoint.
    private final Map<String, Object> connLock = new HashMap<String, Object>();

    private boolean init = false;

    private ClientConfigurationBean clientConfigurationBean;

    @Override
    public synchronized void initPool(ClientConfigurationBean clientConfigurationBean) {
        if (init) {
            return;
        }
        log.debug("init godfs java client pool.");
        init = true;
        this.clientConfigurationBean = clientConfigurationBean;
    }

    @Override
    public synchronized void initEndPoint(EndPoint endPoint, int maxConnPerServer) {
        log.debug("init endPoint: {}:{}", endPoint.getHost(), endPoint.getPort());
        connConfig.put(endPoint.getUuid(), maxConnPerServer);
        LinkedList<Bridge> connList = conns.get(endPoint.getUuid());
        Object lock = connLock.get(endPoint.getUuid());
        Integer _maxConnPerServer = connConfig.get(endPoint.getUuid());
        Integer current = currentConns.get(endPoint.getUuid());

        if (null == connList) {
            connList = new LinkedList<Bridge>();
            conns.put(endPoint.getUuid(), connList);
        }
        if (null == lock) {
            lock = new Object();
            connLock.put(endPoint.getUuid(), lock);
        }
        if (null == _maxConnPerServer) {
            connConfig.put(endPoint.getUuid(), maxConnPerServer);
        }
        if (null == current) {
            currentConns.put(endPoint.getUuid(), 0);
        }
    }

    @Override
    public Bridge getBridge(EndPoint endPoint) throws Exception {
        log.debug("fetch connection bridge for endPoint: {}:{}", endPoint.getHost(), endPoint.getPort());
        Object lock = connLock.get(endPoint.getUuid());
        synchronized (lock) {
            LinkedList<Bridge> connList = conns.get(endPoint.getUuid());
            if (connList.size() > 0) {
                return connList.remove(0);
            }
            log.debug("no connection bridge available for endPoint: {}:{}, create a new connection.", endPoint.getHost(), endPoint.getPort());
            return createNewBridge(endPoint);
        }
    }


    private Bridge createNewBridge(EndPoint endPoint) throws Exception {
        log.debug("create connection bridge for endPoint: {}:{}", endPoint.getHost(), endPoint.getPort());
        Socket s = new Socket(endPoint.getHost(), endPoint.getPort());
        Bridge bridge = new Bridge(s);
        try {
            bridge.validateConnection(clientConfigurationBean.getSecret());
            increaseActiveConnection(endPoint, 1);
            return bridge;
        } catch (Exception e) {
            bridge.close();
            throw e;
        }
    }

    @Override
    public void returnBridge(EndPoint endPoint, Bridge bridge) {
        if (null == bridge) {
            return;
        }
        log.debug("return health connection bridge for endPoint: {}:{}", endPoint.getHost(), endPoint.getPort());
        Object lock = connLock.get(endPoint.getUuid());
        synchronized (lock) {
            LinkedList<Bridge> connList = conns.get(endPoint.getUuid());
            connList.add(bridge);
        }
    }

    @Override
    public void returnBrokenBridge(EndPoint endPoint, Bridge bridge) {
        if (null == bridge) {
            return;
        }
        log.debug("return broken connection bridge for endPoint: {}:{}", endPoint.getHost(), endPoint.getPort());
        increaseActiveConnection(endPoint, -1);
        bridge.close();
    }

    @Override
    public synchronized int increaseActiveConnection(EndPoint endPoint, int value) {
        Integer current = currentConns.get(endPoint.getUuid());
        current += value;
        currentConns.put(endPoint.getUuid(), current);
        return current;
    }
}
