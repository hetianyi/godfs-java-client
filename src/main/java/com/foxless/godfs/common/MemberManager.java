package com.foxless.godfs.common;

import com.alibaba.fastjson.JSON;
import com.foxless.godfs.bean.Member;
import com.foxless.godfs.bean.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MemberManager {
    private static final Logger log = LoggerFactory.getLogger(MemberManager.class);

    private static Map<Tracker, Set<Member>> managedMembers = new HashMap<Tracker, Set<Member>>();

    public static void refresh(Tracker tracker, Set<Member> members) {
        if (null == members || members.isEmpty()) {
            synchronized (managedMembers) {
                log.debug("add members: {} of tracker: {}:{}", JSON.toJSONString(members), tracker.getHost(), tracker.getPort());
                managedMembers.put(tracker, members);
            }
        }
    }

    public static Set<Member> getMembers(Tracker tracker) {
        if (null != tracker) {
            return managedMembers.get(tracker);
        }
        Set<Member> allMember = new HashSet<>();
        synchronized (managedMembers) {
            Collection<Set<Member>> collections = managedMembers.values();
            if (null == collections || collections.isEmpty()) {
                return null;
            }
            for (Iterator<Set<Member>> it = collections.iterator(); it.hasNext();) {
                Set<Member> set = it.next();
                if (null == set) {
                    continue;
                }
                for (Member m : set) {
                    allMember.add(m);
                }
            }
        }
        return allMember;
    }


    public static Set<Member> getNoReadOnlyMembers(Tracker tracker) {
        if (null != tracker) {
            Set<Member> mems = managedMembers.get(tracker);
            if (null != mems) {
                Set<Member> ret = new HashSet<>(mems.size());
                for (Member m : mems) {
                    if (!m.isReadonly()) {
                        ret.add(m);
                    }
                }
                return ret;
            }
            return null;
        }
        Set<Member> allMember = new HashSet<>();
        synchronized (managedMembers) {
            Collection<Set<Member>> collections = managedMembers.values();
            if (null == collections || collections.isEmpty()) {
                return null;
            }
            for (Iterator<Set<Member>> it = collections.iterator(); it.hasNext();) {
                Set<Member> set = it.next();
                if (null == set) {
                    continue;
                }
                for (Member m : set) {
                    if (!m.isReadonly()) {
                        allMember.add(m);
                    }
                }
            }
        }
        return allMember;
    }
}
