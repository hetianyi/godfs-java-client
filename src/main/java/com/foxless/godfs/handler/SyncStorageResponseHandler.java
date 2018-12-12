package com.foxless.godfs.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxless.godfs.bean.ExpireMember;
import com.foxless.godfs.bean.Member;
import com.foxless.godfs.bean.Meta;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.bean.meta.OperationGetStorageServerResponse;
import com.foxless.godfs.common.*;
import com.foxless.godfs.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class SyncStorageResponseHandler implements IResponseHandler {

    private static final Logger log = LoggerFactory.getLogger(SyncStorageResponseHandler.class);

    @Override
    public Object handle(Bridge bridge, Tracker tracker, Meta meta, IReader byteReceiver) throws Exception {
        if (meta.getError() != null) {
            throw meta.getError();
        }
        ObjectMapper objectMapper = Utils.getObjectMapper();
        OperationGetStorageServerResponse response = objectMapper.readValue(new String(meta.getMetaBody()), OperationGetStorageServerResponse.class);
        log.debug("response status {} from server.", response.getStatus());
        if (response.getStatus() == Const.STATUS_OK) {
            if (null != response.getMembers()) {
                Set<ExpireMember> members = new HashSet<ExpireMember>(response.getMembers().length);
                for (Member m : response.getMembers()) {
                    members.add(new ExpireMember().from(m));
                }
                MemberManager.refresh(tracker, members);
            }
        } else {
            log.error("tracker server {}:{} response err status {}", bridge.getConnection().getInetAddress().getHostAddress(), bridge.getConnection().getPort(), response.getStatus());
            throw new IllegalStateException("STATUS_BAD_SECRET");
        }
        return null;
    }
}
