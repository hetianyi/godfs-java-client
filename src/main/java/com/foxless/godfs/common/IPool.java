package com.foxless.godfs.common;

import com.foxless.godfs.bean.Member;

public interface IPool {
    void init(int maxConnPerServer);
    Bridge getConnBridge(Member member);
    void returnConnBridge(Member member, Bridge bridge);
    void increaseActiveConnection(Member member, int value);
}
