package com.foxless.godfs.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StorageDO {

    private String uuid;

    private String host;

    private Integer port;

    @JsonProperty("advertise_addr")
    private String advertiseAddr;

    @JsonProperty("advertise_port")
    private Integer advertisePort;

    private Integer status;

    private String group;

    @JsonProperty("instance_id")
    private String instanceId;

    @JsonProperty("http_port")
    private Integer httpPort;

    @JsonProperty("http_enable")
    private boolean httpEnable;

    @JsonProperty("start_time")
    private Long startTime;

    private Long downloads;

    private Long uploads;

    private Long disk;

    @JsonProperty("read_only")
    private boolean readOnly;

    @JsonProperty("total_files")
    private Integer totalFiles;

    private Integer finish;

    private Long ioin;

    private Long ioout;

    @JsonProperty("access_flag")
    private Integer accessFlag;

    @JsonProperty("log_time")
    private Long logTime;

    @JsonProperty("expire_time")
    private Long serverExpireTime;

    private Integer stageDownloads;

    private Integer stageUploads;

    private Long stageIOin;

    private Long stageIOout;

    private Long mem;

    private long expireTime;


    public boolean isExpired(long time) {
        return this.expireTime < time;
    }


    // -------------getters and setters-------------

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public boolean isHttpEnable() {
        return httpEnable;
    }

    public void setHttpEnable(boolean httpEnable) {
        this.httpEnable = httpEnable;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getDownloads() {
        return downloads;
    }

    public void setDownloads(Long downloads) {
        this.downloads = downloads;
    }

    public Long getUploads() {
        return uploads;
    }

    public void setUploads(Long uploads) {
        this.uploads = uploads;
    }

    public Long getDisk() {
        return disk;
    }

    public void setDisk(Long disk) {
        this.disk = disk;
    }

    public Integer getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(Integer totalFiles) {
        this.totalFiles = totalFiles;
    }

    public Integer getFinish() {
        return finish;
    }

    public void setFinish(Integer finish) {
        this.finish = finish;
    }

    public Long getIoin() {
        return ioin;
    }

    public void setIoin(Long ioin) {
        this.ioin = ioin;
    }

    public Long getIoout() {
        return ioout;
    }

    public void setIoout(Long ioout) {
        this.ioout = ioout;
    }

    public Integer getAccessFlag() {
        return accessFlag;
    }

    public void setAccessFlag(Integer accessFlag) {
        this.accessFlag = accessFlag;
    }

    public Long getLogTime() {
        return logTime;
    }

    public void setLogTime(Long logTime) {
        this.logTime = logTime;
    }

    public Long getServerExpireTime() {
        return serverExpireTime;
    }

    public void setServerExpireTime(Long serverExpireTime) {
        this.serverExpireTime = serverExpireTime;
    }

    public Integer getStageDownloads() {
        return stageDownloads;
    }

    public void setStageDownloads(Integer stageDownloads) {
        this.stageDownloads = stageDownloads;
    }

    public Integer getStageUploads() {
        return stageUploads;
    }

    public void setStageUploads(Integer stageUploads) {
        this.stageUploads = stageUploads;
    }

    public Long getStageIOin() {
        return stageIOin;
    }

    public void setStageIOin(Long stageIOin) {
        this.stageIOin = stageIOin;
    }

    public Long getStageIOout() {
        return stageIOout;
    }

    public void setStageIOout(Long stageIOout) {
        this.stageIOout = stageIOout;
    }

    public Long getMem() {
        return mem;
    }

    public void setMem(Long mem) {
        this.mem = mem;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
}
