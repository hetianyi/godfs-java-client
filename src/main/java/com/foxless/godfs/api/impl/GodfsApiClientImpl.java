package com.foxless.godfs.api.impl;

import com.foxless.godfs.api.GodfsApiClient;
import com.foxless.godfs.bean.*;
import com.foxless.godfs.bean.meta.OperationGetStorageServerRequest;
import com.foxless.godfs.bean.meta.OperationQueryFileRequest;
import com.foxless.godfs.bean.meta.OperationUploadFileRequest;
import com.foxless.godfs.common.*;
import com.foxless.godfs.config.ClientConfigurationBean;
import com.foxless.godfs.handler.QueryFileResponseHandler;
import com.foxless.godfs.handler.SyncStorageResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * godfs api client implementation.
 * for file upload, file download, file query etc.
 *
 * @author hehety
 * @sine 1.0
 * @date 2018/09/26
 * @version 1.0
 */
public class GodfsApiClientImpl implements GodfsApiClient {

    private static final Logger log = LoggerFactory.getLogger(GodfsApiClientImpl.class);

    private static GodfsApiClientImpl godfsApiClientInstance;

    private final ClientConfigurationBean configuration;

    private GodfsApiClientImpl(ClientConfigurationBean configuration) {
        this.configuration = configuration;
    }

    public static synchronized final GodfsApiClient getInstance(ClientConfigurationBean configuration) {
        if (null == godfsApiClientInstance) {
            godfsApiClientInstance = new GodfsApiClientImpl(configuration);
        }
        return godfsApiClientInstance;
    }

    @Override
    public File query(String pathOrMd5) throws Exception {
        if (null == pathOrMd5 || "".equals(pathOrMd5)) {
            log.warn("query parameter cannot be null or empty");
            return null;
        }
        pathOrMd5 = pathOrMd5.trim();
        if (!pathOrMd5.matches(Const.PATH_REGEX) && !pathOrMd5.matches(Const.MD5_REGEX)) {
            log.warn("query parameter mismatch pattern");
            return null;
        }
        if (pathOrMd5.indexOf("/") != -1) {
            pathOrMd5 = "/" + pathOrMd5;
        }

        if (null != configuration.getTrackers()) {
            for (Tracker tracker : configuration.getTrackers()) {
                EndPoint endPoint = EndPoint.fromTracker(tracker);
                Const.getPool().initEndPoint(endPoint, tracker.getMaxConnections());
                Bridge bridge = null;
                boolean broken = false;
                try {
                    log.debug("query file '{}' from tracker server: {}:{}", pathOrMd5, tracker.getHost(), tracker.getPort());
                    bridge = Const.getPool().getBridge(endPoint);
                    OperationQueryFileRequest queryFileRequest = new OperationQueryFileRequest();
                    queryFileRequest.setMd5(pathOrMd5);
                    bridge.sendRequest(Const.O_QUERY_FILE, queryFileRequest, 0, null);
                    File file = (File) bridge.receiveResponse(tracker, QueryFileResponseHandler.class);
                    if (null != file) {
                        return file;
                    }
                    continue;
                } catch (Exception e) {
                    broken = true;
                    log.error("error query file from tracker server [{}:{}] due to: {}", tracker.getHost(), tracker.getPort(), e.getMessage());
                    throw e;
                } finally {
                    if (broken) {
                        Const.getPool().returnBrokenBridge(endPoint, bridge);
                    } else {
                        Const.getPool().returnBridge(endPoint, bridge);
                    }
                }
            }
        } else {
            return null;
        }
        return null;
    }

    @Override
    public String upload(InputStream ips, long fileSize, String group, IMonitor<MonitorProgressBean> monitor) throws Exception {
        Set<ExpireMember> members = MemberManager.getMembersByGroup(group, false);
        if (null == members || members.isEmpty()) {
            throw new IllegalStateException("No storage server available[1].");
        }

        ExpireMember member;
        Bridge connBridge;
        Set<ExpireMember> excludes = null;
        while(true) {
            member = selectStorageMember(members, excludes, null, true);
            if (null == member) {
                throw new IllegalStateException("No storage server available[2].");
            }
            try {
                log.debug("try to get connection for storage server[{}:{}].", member.getAddr(), member.getPort());
                connBridge = Const.getPool().getBridge(member.getEndPoint());
                break;
            } catch (Exception e) {
                if (null == excludes) {
                    excludes = new HashSet<>(2);
                    excludes.add(member);
                }
                log.error("error getting connection for storage server[{}:{}] duo to: {}", member.getAddr(), member.getPort(), e.getMessage());
            }
        }
        OperationUploadFileRequest uploadFileRequest = new OperationUploadFileRequest();
        uploadFileRequest.setFileSize(fileSize);
        uploadFileRequest.setExt("");
        uploadFileRequest.setMd5("");
        UploadStreamWriter writer = new UploadStreamWriter(ips, monitor);

        connBridge.sendRequest(Const.O_UPLOAD, uploadFileRequest, 0, writer);
        //TODO to be continue here
        connBridge.receiveResponse(null, SyncStorageResponseHandler.class);

        byte[] buffer = new byte[10240];
        int len;
        long finish = 0l;
        while((len = ips.read(buffer)) != -1) {
            if (null != progressBean) {
                finish += len;
                progressBean.setFinish(finish);
                monitor.monitor(progressBean);
            }
            // write
        }
        return null;
    }

    private final ClientConfigurationBean getConfiguration() {
        return configuration;
    }


    /**
     * select a storage server.
     * if forUpload is true, the the picked member should not be readonly.
     * @param members
     * @param exclude
     * @param instanceId
     * @param forUpload
     * @return
     */
    private ExpireMember selectStorageMember(Set<ExpireMember> members, Set<ExpireMember> exclude, String instanceId, boolean forUpload) {
        ExpireMember theone = null;
        for (ExpireMember m : members) {
            if (null != exclude && exclude.contains(m)) {
                continue;
            }
            if (null != instanceId && !"".equals(instanceId) && Objects.equals(m.getInstance_id(), instanceId)) {
                return m;
            }
            if (forUpload && m.isReadonly()) {
                continue;
            }
            if (null == theone) {
                theone = m;
                continue;
            }
            Long weight1 = MemberManager.getWeight(EndPoint.fromMember(m));
            Long weight2 = MemberManager.getWeight(EndPoint.fromMember(theone));
            if (weight1 < weight2) {
                theone = m;
            }
        }
        return theone;
    }


}
