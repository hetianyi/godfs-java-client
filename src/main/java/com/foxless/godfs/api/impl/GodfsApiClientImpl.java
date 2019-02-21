package com.foxless.godfs.api.impl;

import com.foxless.godfs.ClientConfigurationBean;
import com.foxless.godfs.api.GodfsApiClient;
import com.foxless.godfs.bridge.TcpBridgeClient;
import com.foxless.godfs.bridge.meta.QueryFileMeta;
import com.foxless.godfs.bridge.meta.QueryFileResponseMeta;
import com.foxless.godfs.client.MemberManager;
import com.foxless.godfs.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

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

    private static final Logger logger = LoggerFactory.getLogger(GodfsApiClientImpl.class);

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
    public FileVO query(String pathOrMd5) throws Exception {
        if (null == pathOrMd5 || "".equals(pathOrMd5)) {
            logger.error("query parameter cannot be null or empty");
            return null;
        }
        pathOrMd5 = pathOrMd5.trim();
        if (!pathOrMd5.matches(Const.PATH_REGEX) && !pathOrMd5.matches(Const.MD5_REGEX)) {
            logger.error("query parameter mismatch pattern");
            return null;
        }
        if (pathOrMd5.indexOf("/") != -1) {
            pathOrMd5 = "/" + pathOrMd5;
        }

        if (null != configuration.getTrackers()) {
            for (Tracker tracker : configuration.getTrackers()) {
                try {
                    logger.debug("query fileEntity '{}' from tracker server: {}:{}", pathOrMd5, tracker.getHost(), tracker.getPort());
                    ServerInfo info = ServerInfo.fromTracker(tracker);
                    info.setSecret(tracker.getSecret());
                    TcpBridgeClient client = new TcpBridgeClient(info);
                    client.connect();
                    client.validate();
                    QueryFileMeta meta = new QueryFileMeta();
                    meta.setPathMd5(pathOrMd5);
                    QueryFileResponseMeta respMeta = client.queryFile(meta);
                    if (respMeta.isExist()) {
                        return respMeta.getFile();
                    }
                    logger.debug("cannot find file from tracker server {}:{}", tracker.getHost(), tracker.getPort());
                    continue;
                } catch (Exception e) {
                    logger.error("error query file from tracker server [{}:{}] due to: {}", tracker.getHost(), tracker.getPort(), e.getMessage());
                    continue;
                }
            }
        } else {
            return null;
        }
        return null;
    }


    @Override
    public String upload(InputStream ips, long fileSize) throws Exception {
        return upload(ips, fileSize, null, null);
    }

    @Override
    public String upload(InputStream ips, long fileSize, String group, IMonitor<MonitorProgressBean> monitor) throws Exception {

        Tuple tuple = selectUsableStorageMember(group, true);
        ExpireMember member = tuple.getF();
        Bridge connBridge = tuple.getS();

        OperationUploadFileRequest uploadFileRequest = new OperationUploadFileRequest();
        uploadFileRequest.setFileSize(fileSize);
        uploadFileRequest.setExt("");
        uploadFileRequest.setMd5("");
        UploadStreamWriter writer = new UploadStreamWriter(ips, monitor);

        boolean broken = false;
        try {
            connBridge.sendRequest(Const.O_UPLOAD, uploadFileRequest, fileSize, writer);
            return (String) connBridge.receiveResponse(null, UploadResponseHandler.class, null);
        } catch (Exception e) {
            broken = true;
            throw e;
        } finally {
            if (broken) {
                Const.getPool().returnBrokenBridge(member.getEndPoint(), connBridge);
            } else {
                Const.getPool().returnBridge(member.getEndPoint(), connBridge);
            }
        }
    }

    @Override
    public String upload(File file) throws Exception {
        return upload(file, null, null);
    }

    @Override
    public String upload(File file, String group, IMonitor<MonitorProgressBean> monitor) throws Exception {
        InputStream ips = null;
        try {
            ips = new FileInputStream(file);
            return upload(ips, file.length(), group, monitor);
        } finally {
            if (null != ips) {
                ips.close();
            }
        }
    }

    @Override
    public String upload(HttpServletRequest request, String group, IMonitor<MonitorProgressBean> monitor, String protocol) throws Exception {
        protocol = null == protocol ? "http" : protocol.toLowerCase();
        if (!"http".equals(protocol) && !"https".equals(protocol)) {
            protocol = "http";
        }

        Set<ExpireMember> members = MemberManager.getMembersByGroup(group, false);
        if (null == members || members.isEmpty()) {
            throw new IllegalStateException("no http storage server available[1].");
        }
        Set<ExpireMember> excludes = new HashSet<ExpireMember>(members.size());
        for (;;) {
            boolean dutyStream = false;
            ExpireMember theOne = null;
            for (ExpireMember m : members) {
                if (!m.isHttpEnable()) {
                    log.debug("storage server {}:{} not support http upload, skip", m.getAddr(), m.getPort());
                    continue;
                }
                if (excludes.contains(m)) {
                    continue;
                }
                if (null == theOne) {
                    theOne = m;
                    continue;
                }
                Long weight1 = MemberManager.getWeight(EndPoint.fromMember(m));
                Long weight2 = MemberManager.getWeight(EndPoint.fromMember(theOne));
                if (weight1 < weight2) {
                    theOne = m;
                }
            }
            if (null == theOne) {
                throw new IllegalStateException("no http storage server available[2].");
            }
            excludes.add(theOne);
            MemberManager.increaseWeight(EndPoint.fromMember(theOne), 1);
            OutputStream ops = null;
            HttpURLConnection connection = null;
            InputStream rips = null;
            try {
                URL url = new URL(protocol + "://" + theOne.getAddr() + ":" + theOne.getHttpPort() +"/upload");
                log.debug("uploading file to: {}", url.toString());
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(Const.BUFFER_SIZE);
                connection.setRequestMethod("POST");
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    log.debug("upload header >> {}:{}", name, request.getHeader(name));
                    connection.addRequestProperty(name, request.getHeader(name));
                }
                connection.connect();
                ops = connection.getOutputStream();
                InputStream ips = request.getInputStream();
                byte[] buffer = new byte[Const.BUFFER_SIZE];
                int len ;
                log.debug("begin to read form stream");
                while((len = ips.read(buffer)) != -1) {
                    dutyStream = true;
                    ops.write(buffer, 0, len);
                    ops.flush();
                }
                log.debug("bytes send success, reading response from server");
                rips = connection.getInputStream();
                StringBuffer sb = new StringBuffer();
                while((len = rips.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, len));
                }
                rips.close();
                log.debug("upload finish, response is [{}]", sb.toString());
                return sb.toString();
            } catch (Exception e) {
                //e.printStackTrace();
                log.info("connection error with storage server {}:{} duo to: {}", theOne.getAddr(), theOne.getHttpPort(), e.getMessage());
                if (dutyStream) {
                    break;
                } else {
                    continue;
                }
            } finally {
                if (null != connection) {
                    connection.disconnect();
                }
                if (null != ops) {
                    ops.close();
                }
                if (null != ops) {
                    ops.close();
                }
                if (null != rips) {
                    rips.close();
                }
            }
        }
        return null;
    }

    @Override
    public String upload(HttpServletRequest request) throws Exception {
        return upload(request, null, null, null);
    }

    @Override
    public void download(String path, long start, long offset, IReader byteReceiver) throws Exception {
        if (null == byteReceiver) {
            throw new IllegalArgumentException("parameter 'byteReceiver' cannot be null");
        }
        if (null == path || "".equals(path)) {
            throw new IllegalArgumentException("parameter 'path' cannot be null or empty");
        }
        path = path.trim();
        if (!path.matches(Const.PATH_REGEX)) {
            log.warn("parameter 'path' mismatch pattern");
        }
        if (path.indexOf("/") != -1) {
            path = "/" + path;
        }


        Tuple tuple = selectUsableStorageMember(null, false);
        ExpireMember member = tuple.getF();
        Bridge connBridge = tuple.getS();

        OperationDownloadFileRequest downloadFileRequest = new OperationDownloadFileRequest();
        downloadFileRequest.setPath(path);
        downloadFileRequest.setStart(start);
        downloadFileRequest.setOffset(offset);


        boolean broken = false;
        try {
            log.debug("download {}", path);
            connBridge.sendRequest(Const.O_DOWNLOAD_FILE, downloadFileRequest, 0, null);
            connBridge.receiveResponse(null, DownloadFileResponseHandler.class, byteReceiver);
        } catch (Exception e) {
            if (e.getClass() == FileNotFoundException.class) {
                throw e;
            } else {
                broken = true;
            }
            throw e;
        } finally {
            if (broken) {
                Const.getPool().returnBrokenBridge(member.getEndPoint(), connBridge);
            } else {
                Const.getPool().returnBridge(member.getEndPoint(), connBridge);
            }
        }
    }

    @Override
    public void download(String path, IReader byteReceiver) throws Exception {
        this.download(path, 0, -1, byteReceiver);
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
    private StorageDO selectStorageMember(Set<StorageDO> members, Set<StorageDO> exclude, String instanceId, boolean forUpload) {
        StorageDO theOne = null;
        for (StorageDO m : members) {
            if (null != exclude && exclude.contains(m)) {
                continue;
            }
            if (null != instanceId && !"".equals(instanceId) && Objects.equals(m.getInstance_id(), instanceId)) {
                return m;
            }
            if (forUpload && m.isReadOnly()) {
                continue;
            }
            if (null == theOne) {
                theOne = m;
                continue;
            }
            Long weight1 = MemberManager.getWeight(m.getUuid());
            Long weight2 = MemberManager.getWeight(m.getUuid());
            if (weight1 < weight2) {
                theOne = m;
            }
        }
        return theOne;
    }



    private ObjectTuple<StorageDO, TcpBridgeClient> selectUsableStorageMember(String group, boolean forUpload) {
        Set<StorageDO> excludes = null;
        StorageDO member;
        Bridge connBridge;
        // select specify nodes which match the group and can upload
        Set<StorageDO> members = MemberManager.getMembersByGroup(forUpload ? group : null, false);
        if (null == members || members.isEmpty()) {
            throw new IllegalStateException("no storage server available [1].");
        }
        while(true) {
            // select from members by weight
            member = selectStorageMember(members, excludes, null, forUpload);
            if (null == member) {
                throw new IllegalStateException("no storage server available [4].");
            }
            try {
                ServerInfo info = ServerInfo.fromTracker()
                logger.debug("try to get connection for storage server[{}:{}].", member.getAddr(), member.getPort());
                connBridge = Const.getPool().getBridge(member.getEndPoint());
                return new Tuple(member, connBridge);
            } catch (Exception e) {
                if (null == excludes) {
                    excludes = new HashSet<>(2);
                    excludes.add(member);
                }
                logger.error("error getting connection for storage server[{}:{}] duo to: {}", member.getAddr(), member.getPort(), e.getMessage());
            } finally {
                MemberManager.increaseWeight(EndPoint.fromMember(member), 1);
            }
        }
    }


    private class Tuple {
        private ExpireMember f;
        private Bridge s;

        public Tuple(ExpireMember f, Bridge s) {
            this.f = f;
            this.s = s;
        }

        public ExpireMember getF() {
            return f;
        }

        public void setF(ExpireMember f) {
            this.f = f;
        }

        public Bridge getS() {
            return s;
        }

        public void setS(Bridge s) {
            this.s = s;
        }
    }

}
