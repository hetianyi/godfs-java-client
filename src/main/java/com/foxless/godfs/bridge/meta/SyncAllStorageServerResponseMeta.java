package com.foxless.godfs.bridge.meta;

import com.foxless.godfs.common.StorageDO;

public class SyncAllStorageServerResponseMeta {
    private StorageDO[] servers;

    public StorageDO[] getServers() {
        return servers;
    }

    public void setServers(StorageDO[] servers) {
        this.servers = servers;
    }
}
