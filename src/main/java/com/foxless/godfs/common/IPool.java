package com.foxless.godfs.common;

import com.foxless.godfs.bean.EndPoint;
import com.foxless.godfs.config.ClientConfigurationBean;

/**
 * connection pool manager for endpoint.
 */
public interface IPool {
    /**
     * init connection pool
     */
    void initPool(ClientConfigurationBean clientConfigurationBean);
    /**
     * init config of a EndPoint
     * @param maxConnPerServer max connection count for each EndPoint
     */
    void initEndPoint(EndPoint endPoint, int maxConnPerServer);

    /**
     * get connection bridge by EndPoint
     */
    Bridge getBridge(EndPoint endPoint) throws Exception;

    /**
     * return a health connection bridge
     */
    void returnBridge(EndPoint endPoint, Bridge bridge);

    /**
     * return a broken connection bridge
     */
    void returnBrokenBridge(EndPoint endPoint, Bridge bridge);

    /**
     * increase active connection count of specific EndPoint with given value and return active connection count now.
     */
    int increaseActiveConnection(EndPoint endPoint, int value);
}
