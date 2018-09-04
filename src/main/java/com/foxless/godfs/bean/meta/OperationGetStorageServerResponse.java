package com.foxless.godfs.bean.meta;

import com.foxless.godfs.bean.Member;

public class OperationGetStorageServerResponse {
    private int status;
    private Member[] members;// 我的组内成员（不包括自己）

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Member[] getMembers() {
        return members;
    }

    public void setMembers(Member[] members) {
        this.members = members;
    }
}
