package com.foxless.godfs.common;

public class ServerInfo {


    public static final int AccessFlagNone = 0;
    public static final int AccessFlagInitial   = 1;
    public static final int AccessFlagAdvertise = 2;

    private String host;
    private Integer port;
    private String group;
    private String instanceId;
    private Integer accessFlag;
    private String advertiseAddr;
    private Integer advertisePort;
    private Boolean tracker;


    public static ServerInfo fromConnStr(String connStr) {
        ServerInfo server = new ServerInfo();
        server.host = connStr.split(":")[0];
        server.port = Integer.valueOf(connStr.split(":")[1]);
        server.tracker = true;
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


}
