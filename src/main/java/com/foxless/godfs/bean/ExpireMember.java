package com.foxless.godfs.bean;

import java.util.Date;

public class ExpireMember {
    private String addr;
    private String instance_id;
    private String group;
    private int port;
    private boolean readonly;
    private Date expireTime;
    private EndPoint endPoint;

    public ExpireMember from(Member member) {
        this.addr = member.getAddr();
        this.instance_id = member.getInstance_id();
        this.group = member.getGroup();
        this.port = member.getPort();
        this.readonly = member.isReadonly();
        this.endPoint = EndPoint.fromMember(member);
        return this;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getInstance_id() {
        return instance_id;
    }

    public void setInstance_id(String instance_id) {
        this.instance_id = instance_id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public EndPoint getEndPoint() {
        return endPoint;
    }

    public boolean isExpired(Date now) {
        if (this.expireTime.getTime() < now.getTime()) {
            return true;
        }
        return false;
    }
}
