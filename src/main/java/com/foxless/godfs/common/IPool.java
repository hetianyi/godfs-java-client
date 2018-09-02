package com.foxless.godfs.common;

import com.foxless.godfs.bean.Bridge;
import com.foxless.godfs.bean.Member;

public interface IPool {
    void init();
    Bridge getConnBridge(Member member);
    void returnConnBridge(Member member, Bridge bridge);
    void increaseActiveConnection(Member member, int value);
}
