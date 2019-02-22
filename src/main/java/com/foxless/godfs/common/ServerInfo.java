package com.foxless.godfs.common;

public class ServerInfo {


    public static final int AccessFlagNone = 0;
    public static final int AccessFlagInitial   = 1;
    public static final int AccessFlagAdvertise = 2;

    private String host;
    private int port;
    private String group;
    private String instanceId;
    private String secret;
    private int accessFlag;
    private String advertiseAddr;
    private int advertisePort;
    private boolean tracker;


    public static ServerInfo fromConnStr(String connStr) {
        ServerInfo server = new ServerInfo();
        server.host = connStr.split(":")[0];
        server.port = Integer.valueOf(connStr.split(":")[1]);
        server.tracker = true;
        return server;
    }

    public static ServerInfo fromTracker(Tracker tracker) {
        ServerInfo server = new ServerInfo();
        server.host = tracker.getHost();
        server.port = tracker.getPort();
        server.tracker = true;
        server.secret = tracker.getSecret();
        return server;
    }

    public static ServerInfo fromStorage(StorageDO storageDO) {
        ServerInfo server = new ServerInfo();
        server.host = storageDO.getHost();
        server.port = storageDO.getPort();
        server.advertiseAddr = storageDO.getAdvertiseAddr();
        server.advertisePort = storageDO.getAdvertisePort();
        server.group = storageDO.getGroup();
        server.instanceId = storageDO.getInstanceId();
        server.tracker = true;
        server.secret = storageDO.getSecret();
        return server;
    }

    public void SwitchAccessFlag() {
        if (this.accessFlag == AccessFlagInitial) {
            this.accessFlag = AccessFlagAdvertise;
        } else {
            this.accessFlag = AccessFlagInitial;
        }
    }

    public AddrTuple GetHostAndPortByAccessFlag() {
        if (this.tracker) {
            this.advertiseAddr = this.host;
            this.advertisePort = this.port;
            return new AddrTuple(this.host, this.port);
        }
        if (this.accessFlag == AccessFlagNone) {
            // if run as client, always try from advertise ip
            this.accessFlag = AccessFlagAdvertise;
            return new AddrTuple(this.advertiseAddr, this.advertisePort);
        }
        if (this.accessFlag == AccessFlagInitial) {
            return new AddrTuple(this.host, this.port);
        }
        return new AddrTuple(this.advertiseAddr, this.advertisePort);
    }


    // -------------getters and setters-------------

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Integer getAccessFlag() {
        return accessFlag;
    }

    public void setAccessFlag(Integer accessFlag) {
        this.accessFlag = accessFlag;
    }

    public String getAdvertiseAddr() {
        return advertiseAddr;
    }

    public void setAdvertiseAddr(String advertiseAddr) {
        this.advertiseAddr = advertiseAddr;
    }

    public Integer getAdvertisePort() {
        return advertisePort;
    }

    public void setAdvertisePort(Integer advertisePort) {
        this.advertisePort = advertisePort;
    }

    public Boolean getTracker() {
        return tracker;
    }

    public void setTracker(Boolean tracker) {
        this.tracker = tracker;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
