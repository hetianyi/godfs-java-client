package com.foxless.godfs.pool;

import com.foxless.godfs.common.AddrTuple;
import com.foxless.godfs.common.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ClientConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(ClientConnectionPool.class);

    private Map<String, List<Socket>> connMap;
    private Map<String, Integer> activeConnCounter;
    private Map<String, Object> serverLock;
    private Integer maxConnPerServer;
    private Integer totalActiveConn;


    public void init(Integer maxConnPerServer) {
        this.connMap = new HashMap<>();
        this.activeConnCounter = new HashMap<>();
        this.serverLock = new HashMap<>();
        if (maxConnPerServer <= 0 || maxConnPerServer > 100) {
            maxConnPerServer = 10;
        }
        this.maxConnPerServer = maxConnPerServer;
    }


    public String getServerKey(ServerInfo server) {
        AddrTuple add = server.GetHostAndPortByAccessFlag();
        return add.getHost() + ":" + add.getPort();
    }

    public synchronized Object getServerLock(ServerInfo server) {
        Object lock = serverLock.get(getServerKey(server));
        if (null == lock) {
            lock = new Object();
            serverLock.put(getServerKey(server), lock);
        }
        return lock;
    }

    public Socket getConn(ServerInfo server) throws Exception {
        Object lock = getServerLock(server);
        synchronized (lock) {
            List<Socket> list = this.getConnMap(server);
            if (list.size() > 0) {
                logger.debug("reuse existing connection");
                return list.remove(0);
            }
            if (this.increaseActiveConnection(server, 0) < this.maxConnPerServer) {
                Socket s = null;
                try {
                    s = this.newConnection(server);
                    return s;
                } catch (Exception e) {
                    logger.debug("switch connection flag to advertise address");
                    server.SwitchAccessFlag();
                    return this.newConnection(server);
                }
            }
            throw new IllegalStateException("connection pool is full");
        }
    }



    // newConnection only connect but not validate this connection
    public Socket newConnection(ServerInfo server) throws Exception {
        AddrTuple add = server.GetHostAndPortByAccessFlag();
        logger.debug("connecting to server " + add.getHost() + ":" + add.getPort() + "...");
        try {
            Socket s = new Socket(add.getHost(), add.getPort());
            this.increaseActiveConnection(server, 1);
            return s;
        } catch (Exception e) {
            // e.printStackTrace();
            logger.debug("error connect to storage server " + add.getHost() + ":" + add.getPort(), ">", e.getMessage());
           throw e;
        }
    }

    // IncreaseActiveConnection increase/decrease the connection size mark if a server
    public synchronized int increaseActiveConnection(ServerInfo server, int value) {
        this.totalActiveConn += value;
        Integer newVal = Optional.ofNullable(this.activeConnCounter.get(getServerKey(server))).orElse(0) + value;
        this.activeConnCounter.put(getServerKey(server), newVal);
        return newVal;
    }

    // getConnMap get server connection managed mapping
    public List<Socket> getConnMap(ServerInfo server) {
        String key = getServerKey(server);
        List<Socket> connList = this.connMap.get(key);
        if (connList == null) {
            connList = new LinkedList<>();
        }
        this.connMap.put(key, connList);
        return connList;
    }


    // ReturnConnBridge finish using tcp connection bridge and return it to connection pool.
    public void ReturnConnBridge(server *app.ServerInfo, conn net.Conn) {
        pool.getLock.Lock()
        defer pool.getLock.Unlock()
        connList := pool.getConnMap(server)
        logger.Debug("return health connection:", connList.Len())
        connList.PushBack(conn)
    }

    // ReturnBrokenConnBridge finish using tcp connection bridge and return it to connection pool.
    func (pool *ClientConnectionPool) ReturnBrokenConnBridge(server *app.ServerInfo, conn net.Conn) {
        pool.getLock.Lock()
        defer pool.getLock.Unlock()
        conn.Close()
        pool.IncreaseActiveConnection(server, -1)
        logger.Trace("return broken connection:", pool.connMap[GetServerKey(server)].Len())
    }

}
