package com.foxless.godfs.api.impl;

import com.foxless.godfs.ClientConfigurationBean;
import com.foxless.godfs.api.GodfsApiClient;
import com.foxless.godfs.bridge.*;
import com.foxless.godfs.bridge.meta.*;
import com.foxless.godfs.client.MemberManager;
import com.foxless.godfs.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
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
                TcpBridgeClient client = null;
                try {
                    logger.debug("query file '{}' from tracker server: {}:{}", pathOrMd5, tracker.getHost(), tracker.getPort());
                    ServerInfo info = ServerInfo.fromTracker(tracker);
                    client = new TcpBridgeClient(info);
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
                    if (null != client) {
                        client.destroy();
                        client = null;
                    }
                    continue;
                } finally {
                    if (null != client) {
                        client.close();
                    }
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

        ObjectTuple<StorageDO, TcpBridgeClient> tuple = selectUsableStorageMember(group, true);
        StorageDO member = tuple.getF();
        final TcpBridgeClient client = tuple.getS();

        UploadFileMeta meta = new UploadFileMeta();
        meta.setFileSize(fileSize);
        meta.setExt("");
        meta.setMd5("");
        boolean err = false;
        try {
            UploadFileResponseMeta respMeta = client.uploadFile(meta, new IHandler() {
                @Override
                public void resolve(ConnectionManager manager, Frame frame) {
                    MonitorProgressBean progress = new MonitorProgressBean();
                    byte[] tmp = new byte[Const.BUFFER_SIZE];
                    int nextRead;
                    long read = 0;
                    try {
                        while (read < fileSize) {
                            if (fileSize - read > Const.BUFFER_SIZE) {
                                nextRead = Const.BUFFER_SIZE;
                            } else {
                                nextRead = (int)(fileSize - read);
                            }
                            read += StreamResolver.readBytes(tmp, nextRead, ips);
                            client.getConnManager().getConn().getOutputStream().write(tmp, 0, nextRead);
                            if (null != monitor) {
                                monitor.monitor(progress);
                            }
                        }
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        client.destroy();
                    }
                }

            });
            if (null == respMeta) {
                throw new IllegalStateException("error upload file to server "
                        + member.getHost() + ":"+ member.getPort() +": cannot get response from server");
            }
            return respMeta.getPath();
        } catch (Exception e) {
            err = true;
            if (null != client) {
                client.destroy();
            }
            throw e;
        } finally {
            if (!err) {
                client.close();
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

        Set<StorageDO> members = MemberManager.getMembersByGroup(group, false);
        if (null == members || members.isEmpty()) {
            throw new IllegalStateException("no http storage server available[1].");
        }
        Set<StorageDO> excludes = new HashSet<StorageDO>(members.size());
        for (;;) {
            boolean dutyStream = false;
            StorageDO theOne = null;
            for (StorageDO m : members) {
                if (!m.isHttpEnable()) {
                    logger.debug("storage server {}:{} not support http upload, skip", m.getHost(), m.getPort());
                    continue;
                }
                if (excludes.contains(m)) {
                    continue;
                }
                if (null == theOne) {
                    theOne = m;
                    continue;
                }
                Long weight1 = MemberManager.getWeight(m.getUuid());
                Long weight2 = MemberManager.getWeight(theOne.getUuid());
                if (weight1 < weight2) {
                    theOne = m;
                }
            }
            if (null == theOne) {
                throw new IllegalStateException("no http storage server available[2].");
            }
            excludes.add(theOne);
            MemberManager.increaseWeight(theOne.getUuid(), 1);
            OutputStream ops = null;
            HttpURLConnection connection = null;
            InputStream rips = null;
            try {
                URL url = new URL(protocol + "://" + theOne.getAdvertiseAddr() + ":" + theOne.getHttpPort() +"/upload");
                logger.debug("uploading file to: {}", url.toString());
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(Const.BUFFER_SIZE);
                connection.setRequestMethod("POST");
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    logger.debug("upload header >> {}:{}", name, request.getHeader(name));
                    connection.addRequestProperty(name, request.getHeader(name));
                }
                connection.connect();
                ops = connection.getOutputStream();
                InputStream ips = request.getInputStream();
                byte[] buffer = new byte[Const.BUFFER_SIZE];
                int len ;
                logger.debug("begin to read form stream");
                while((len = ips.read(buffer)) != -1) {
                    dutyStream = true;
                    ops.write(buffer, 0, len);
                    ops.flush();
                }
                logger.debug("bytes send success, reading response from server");
                rips = connection.getInputStream();
                StringBuffer sb = new StringBuffer();
                while((len = rips.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, len));
                }
                rips.close();
                logger.debug("upload finish, response is [{}]", sb.toString());
                return sb.toString();
            } catch (Exception e) {
                //e.printStackTrace();
                logger.info("connection error with storage server {}:{} duo to: {}", theOne.getAdvertiseAddr(), theOne.getHttpPort(), e.getMessage());
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
    public void download(String path, long start, long offset, IDownloadReader byteReceiver) throws Exception {
        if (null == byteReceiver) {
            throw new IllegalArgumentException("no byteReceiver specified");
        }
        if (null == path || "".equals(path)) {
            throw new IllegalArgumentException("parameter 'path' cannot be null or empty");
        }
        path = path.trim();
        if (!path.matches(Const.PATH_REGEX)) {
            logger.warn("parameter 'path' mismatch pattern");
        }
        if (path.indexOf("/") != -1) {
            path = "/" + path;
        }

        ObjectTuple<StorageDO, TcpBridgeClient> tuple = selectUsableStorageMember(null, false);
        StorageDO member = tuple.getF();
        TcpBridgeClient client = tuple.getS();
        DownloadFileMeta meta = new DownloadFileMeta();
        meta.setPath(path);
        meta.setStart(start);
        meta.setOffset(offset);

        try {
            ObjectTuple<DownloadFileResponseMeta, Frame> retTuple = client.downloadFile(meta);
            if (null == retTuple.getF()) {
                throw new IllegalStateException("error download file from server "
                        + member.getHost() + ":"+ member.getPort() +": cannot get response from server");
            }
            if (!retTuple.getF().isExist()) {
                byteReceiver.before(null);
                byteReceiver.finish();
            } else {
                byteReceiver.before(retTuple.getF().getFile());
                StreamResolver.readFrameBody(retTuple.getS().getBodyLength(), client.getConnManager(), byteReceiver);
                byteReceiver.finish();
            }
        } catch (Exception e) {
            client.destroy();
            client = null;
            throw e;
        } finally {
            if (null != client) {
                client.close();
            }
        }
    }

    @Override
    public void download(String path, IDownloadReader byteReceiver) throws Exception {
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
            if (null != instanceId && !"".equals(instanceId) && Objects.equals(m.getInstanceId(), instanceId)) {
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
            ServerInfo info = ServerInfo.fromStorage(member);
            AddrTuple add = info.GetHostAndPortByAccessFlag();
            TcpBridgeClient client = null;
            try {
                logger.debug("try to get connection for storage server[{}:{}].", add.getHost(), member.getPort());
                client = new TcpBridgeClient(info);
                client.connect();
                client.validate();
                return new ObjectTuple<StorageDO, TcpBridgeClient>(member, client);
            } catch (Exception e) {
                if (null == excludes) {
                    excludes = new HashSet<>(2);
                    excludes.add(member);
                }
                if (null != client) {
                    client.destroy();
                    client = null;
                }
                logger.error("error getting connection for storage server[{}:{}] duo to: {}", add.getHost(), add.getPort(), e.getMessage());
            } finally {
                MemberManager.increaseWeight(member.getUuid(), 1);
            }
        }
    }
}
