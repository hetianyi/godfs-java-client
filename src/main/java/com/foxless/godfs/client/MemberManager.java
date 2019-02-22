package com.foxless.godfs.client;

import com.foxless.godfs.common.StorageDO;
import com.foxless.godfs.common.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * management of storage members
 */
public class MemberManager {

    private static final Logger log = LoggerFactory.getLogger(MemberManager.class);

    private static Map<Tracker, Set<StorageDO>> managedMembers = new HashMap<Tracker, Set<StorageDO>>();

    private static final Map<String, Long> callWeight = new HashMap<String, Long>();

    /**
     * refresh members for tracker server.
     * @param tracker
     * @param members
     */
    public static void refresh(Tracker tracker, Set<StorageDO> members) {
        if (null != members && !members.isEmpty()) {
            synchronized (managedMembers) {
                Set<StorageDO> ret = managedMembers.get(tracker);
                if (null == ret) {
                    ret = new HashSet<>(5);
                    managedMembers.put(tracker, ret);
                }
                boolean hit = false;
                for (StorageDO m1 : members) {
                    for (StorageDO m2 : ret) {
                        // if a storage alive again, add expire time then.
                        if (m1.getUuid() == m2.getUuid()) {
                            hit = true;
                            m2.setExpireTime(System.currentTimeMillis() + TrackerMaintainer.SCHEDULE_INTERVAL*3);
                            log.debug("renewal storage members: {}:{} of tracker: {}:{}", m1.getHost(), m1.getPort(), tracker.getHost(), tracker.getPort());
                            break;
                        }
                    }
                    if (!hit) {
                        log.debug("add storage members: {}:{} of tracker: {}:{}", m1.getHost(), m1.getPort(), tracker.getHost(), tracker.getPort());
                        m1.setExpireTime(System.currentTimeMillis() + TrackerMaintainer.SCHEDULE_INTERVAL * 3);
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
    public static Set<StorageDO> getMembers(Tracker tracker) {
        if (null != tracker) {
            return managedMembers.get(tracker);
        }
        Set<StorageDO> allMember = new HashSet<StorageDO>();
        synchronized (managedMembers) {
            Collection<Set<StorageDO>> collections = managedMembers.values();
            if (null == collections || collections.isEmpty()) {
                return null;
            }
            for (Iterator<Set<StorageDO>> it = collections.iterator(); it.hasNext();) {
                Set<StorageDO> set = it.next();
                if (null == set) {
                    continue;
                }
                for (StorageDO m : set) {
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
    public static Set<StorageDO> getNoReadOnlyMembers(Tracker tracker) {
        if (null != tracker) {
            Set<StorageDO> mems = managedMembers.get(tracker);
            if (null != mems) {
                Set<StorageDO> ret = new HashSet<StorageDO>(mems.size());
                for (StorageDO m : mems) {
                    if (!m.isReadOnly()) {
                        ret.add(m);
                    }
                }
                return ret;
            }
            return null;
        }
        Set<StorageDO> allMember = new HashSet<StorageDO>();
        synchronized (managedMembers) {
            Collection<Set<StorageDO>> collections = managedMembers.values();
            if (null == collections || collections.isEmpty()) {
                return null;
            }
            for (Iterator<Set<StorageDO>> it = collections.iterator(); it.hasNext();) {
                Set<StorageDO> set = it.next();
                if (null == set) {
                    continue;
                }
                for (StorageDO m : set) {
                    if (!m.isReadOnly()) {
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
    public static Set<StorageDO> getMembersByGroup(String group, boolean readonly) {
        Set<StorageDO> allMember = new HashSet<StorageDO>();
        synchronized (managedMembers) {
            Collection<Set<StorageDO>> collections = managedMembers.values();
            if (null == collections || collections.isEmpty()) {
                return null;
            }
            for (Iterator<Set<StorageDO>> it = collections.iterator(); it.hasNext();) {
                Set<StorageDO> set = it.next();
                if (null == set) {
                    continue;
                }
                for (StorageDO m : set) {
                    if ((null == group || Objects.equals(group, m.getGroup()))
                            && Objects.equals(m.isReadOnly(), readonly)) {
                        allMember.add(m);
                    }
                }
            }
        }
        return allMember;
    }


    public static void increaseWeight(String uuid, int value) {
        synchronized (callWeight) {
            Long val = callWeight.get(uuid);
            if (val == null) {
                val = 0L;
                callWeight.put(uuid, val);
            }
            val += value;
            callWeight.put(uuid, val);
        }
    }

    public static long getWeight(String uuid) {
        synchronized (callWeight) {
            return null == callWeight.get(uuid) ? 0l : callWeight.get(uuid);
        }
    }

    public static void expireMember() {
        log.debug("schedule expire members...");
        Date now = new Date();
        synchronized (managedMembers) {
            Collection<Set<StorageDO>> collections = managedMembers.values();
            if (null == collections || collections.isEmpty()) {
                return;
            }
            for (Iterator<Set<StorageDO>> it = collections.iterator(); it.hasNext();) {
                Set<StorageDO> set = it.next();
                if (null == set) {
                    continue;
                }
                for (Iterator<StorageDO> it1 = set.iterator(); it1.hasNext();) {
                    StorageDO em = it1.next();
                    if (em.isExpired(now.getTime())) {
                        log.info("remove member {}:{}", em.getHost(), em.getPort());
                        it1.remove();
                    }
                }
            }
        }
    }

}
