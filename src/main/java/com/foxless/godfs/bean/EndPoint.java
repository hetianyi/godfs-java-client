package com.foxless.godfs.bean;

import java.util.HashMap;
import java.util.Map;

public class EndPoint {

    private static final Map<String, EndPoint> cachedEndPoints = new HashMap<String, EndPoint>();

    private String uuid;
    private String host;
    private int port;


    private EndPoint(Tracker tracker) {
        this.uuid = getUUID(tracker);
        this.host = tracker.getHost();
        this.port = tracker.getPort();
    }
    private EndPoint(Member member) {
        this.uuid = getUUID(member);
        this.host = member.getAddr();
        this.port = member.getPort();
    }
    private EndPoint(ExpireMember expireMember) {
        this.uuid = getUUID(expireMember);
    }

    public static EndPoint fromTracker(Tracker tracker) {
        String uuid = getUUID(tracker);
        EndPoint endPoint = cachedEndPoints.get(uuid);
        if (null != endPoint) {
            return endPoint;
        }
        endPoint = new EndPoint(tracker);
        cachedEndPoints.put(uuid, endPoint);
        return endPoint;
    }
    public static EndPoint fromMember(Member member) {
        String uuid = getUUID(member);
        EndPoint endPoint = cachedEndPoints.get(uuid);
        if (null != endPoint) {
            return endPoint;
        }
        endPoint = new EndPoint(member);
        cachedEndPoints.put(uuid, endPoint);
        return endPoint;
    }
    public static EndPoint fromMember(ExpireMember expireMember) {
        String uuid = getUUID(expireMember);
        EndPoint endPoint = cachedEndPoints.get(uuid);
        if (null != endPoint) {
            return endPoint;
        }
        endPoint = new EndPoint(expireMember);
        cachedEndPoints.put(uuid, endPoint);
        return endPoint;
    }

    public static String getUUID(Object instance) {
        if (null == instance) {
            return null;
        }
        if (instance instanceof Tracker) {
            return ((Tracker) instance).getHost() + ":" + ((Tracker) instance).getPort();
        }
        if (instance instanceof Member) {
            return ((Member) instance).getAddr() + ":" + ((Member) instance).getPort();
        }
        if (instance instanceof ExpireMember) {
            return ((ExpireMember) instance).getAddr() + ":" + ((ExpireMember) instance).getPort();
        }
        return null;
    }

    public String getUuid() {
        return uuid;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

}
