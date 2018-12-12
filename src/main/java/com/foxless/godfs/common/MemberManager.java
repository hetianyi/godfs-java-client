package com.foxless.godfs.common;

import com.alibaba.fastjson.JSON;
import com.foxless.godfs.bean.EndPoint;
import com.foxless.godfs.bean.ExpireMember;
import com.foxless.godfs.bean.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * management of storage members
 */
public class MemberManager {

    private static final Logger log = LoggerFactory.getLogger(MemberManager.class);

    private static Map<Tracker, Set<ExpireMember>> managedMembers = new HashMap<Tracker, Set<ExpireMember>>();

    private static final Map<EndPoint, Long> callWeight = new HashMap<EndPoint, Long>();

    /**
     * refresh members for tracker server.
     * @param tracker
     * @param members
     */
    public static void refresh(Tracker tracker, Set<ExpireMember> members) {
        if (null != members && !members.isEmpty()) {
            synchronized (managedMembers) {
                Set<ExpireMember> ret = managedMembers.get(tracker);
                if (null == ret) {
                    ret = new HashSet<>(5);
                    managedMembers.put(tracker, ret);
                }
                boolean hit = false;
                for (ExpireMember m1 : members) {
                    for (ExpireMember m2 : ret) {
                        // if a storage alive again, add expire time then.
                        if (m1.getEndPoint() == m2.getEndPoint()) {
                            hit = true;
                            m2.setExpireTime(new Date(System.currentTimeMillis() + 30000*3));
                            log.debug("renewal storage members: {}:{} of tracker: {}:{}", m1.getAddr(), m1.getPort(), tracker.getHost(), tracker.getPort());
                            break;
                        }
                    }
                    if (!hit) {
                        log.debug("add storage members: {}:{} of tracker: {}:{}", m1.getAddr(), m1.getPort(), tracker.getHost(), tracker.getPort());
                        ret.add(m1);
                    }
                }
            }
        }
    }

    /**
     * get storage members by tracker.
     * @param tracker
     * @return
     */
    public static Set<ExpireMember> getMembers(Tracker tracker) {
        if (null != tracker) {
            return managedMembers.get(tracker);
        }
        Set<ExpireMember> allMember = new HashSet<ExpireMember>();
        synchronized (managedMembers) {
            Collection<Set<ExpireMember>> collections = managedMembers.values();
            if (null == collections || collections.isEmpty()) {
                return null;
            }
            for (Iterator<Set<ExpireMember>> it = collections.iterator(); it.hasNext();) {
                Set<ExpireMember> set = it.next();
                if (null == set) {
                    continue;
                }
                for (ExpireMember m : set) {
                    allMember.add(m);
                }
            }
        }
        return allMember;
    }

    /**
     * get readonly storage members by tracker.
     * if tracker is null, return all
     * @param tracker
     * @return
     */
    public static Set<ExpireMember> getNoReadOnlyMembers(Tracker tracker) {
        if (null != tracker) {
            Set<ExpireMember> mems = managedMembers.get(tracker);
            if (null != mems) {
                Set<ExpireMember> ret = new HashSet<ExpireMember>(mems.size());
                for (ExpireMember m : mems) {
                    if (!m.isReadonly()) {
                        ret.add(m);
                    }
                }
                return ret;
            }
            return null;
        }
        Set<ExpireMember> allMember = new HashSet<ExpireMember>();
        synchronized (managedMembers) {
            Collection<Set<ExpireMember>> collections = managedMembers.values();
            if (null == collections || collections.isEmpty()) {
                return null;
            }
            for (Iterator<Set<ExpireMember>> it = collections.iterator(); it.hasNext();) {
                Set<ExpireMember> set = it.next();
                if (null == set) {
                    continue;
                }
                for (ExpireMember m : set) {
                    if (!m.isReadonly()) {
                        allMember.add(m);
                    }
                }
            }
        }
        return allMember;
    }

    /**
     * get storage members by group and readonly properties.
     * @param group
     * @param readonly
     * @return
     */
    public static Set<ExpireMember> getMembersByGroup(String group, boolean readonly) {
        Set<ExpireMember> allMember = new HashSet<ExpireMember>();
        synchronized (managedMembers) {
            Collection<Set<ExpireMember>> collections = managedMembers.values();
            if (null == collections || collections.isEmpty()) {
                return null;
            }
            for (Iterator<Set<ExpireMember>> it = collections.iterator(); it.hasNext();) {
                Set<ExpireMember> set = it.next();
                if (null == set) {
                    continue;
                }
                for (ExpireMember m : set) {
                    if (null == group || Objects.equals(group, m.getGroup()) && m.isReadonly() == readonly) {
                        allMember.add(m);
                    }
                }
            }
        }
        return allMember;
    }


    public static void increaseWeight(EndPoint endPoint, int value) {
        synchronized (callWeight) {
            Long val = callWeight.get(endPoint);
            if (val == null) {
                val = 0L;
                callWeight.put(endPoint, val);
            }
            val += value;
            callWeight.put(endPoint, val);
        }
    }

    public static long getWeight(EndPoint endPoint) {
        synchronized (callWeight) {
            return null == callWeight.get(endPoint) ? 0l : callWeight.get(endPoint);
        }
    }

    public static void expireMember() {
        log.debug("expire members........................");
        Date now = new Date();
        synchronized (managedMembers) {
            Collection<Set<ExpireMember>> collections = managedMembers.values();
            if (null == collections || collections.isEmpty()) {
                return;
            }
            for (Iterator<Set<ExpireMember>> it = collections.iterator(); it.hasNext();) {
                Set<ExpireMember> set = it.next();
                if (null == set) {
                    continue;
                }
                for (Iterator<ExpireMember> it1 = set.iterator(); it1.hasNext();) {
                    ExpireMember em = it1.next();
                    if (em.isExpired(now)) {
                        log.info("remove member {}:{}", em.getAddr(), em.getPort());
                        it1.remove();
                    }
                }
            }
        }
    }

}
