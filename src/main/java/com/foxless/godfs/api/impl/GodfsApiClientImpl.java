package com.foxless.godfs.api.impl;

import com.foxless.godfs.api.GodfsApiClient;
import com.foxless.godfs.bean.*;
import com.foxless.godfs.bean.meta.OperationDownloadFileRequest;
import com.foxless.godfs.bean.meta.OperationQueryFileRequest;
import com.foxless.godfs.bean.meta.OperationUploadFileRequest;
import com.foxless.godfs.common.*;
import com.foxless.godfs.config.ClientConfigurationBean;
import com.foxless.godfs.handler.DownloadFileResponseHandler;
import com.foxless.godfs.handler.QueryFileResponseHandler;
import com.foxless.godfs.handler.UploadResponseHandler;
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
    public FileEntity query(String pathOrMd5) throws Exception {
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
                    log.debug("query fileEntity '{}' from tracker server: {}:{}", pathOrMd5, tracker.getHost(), tracker.getPort());
                    bridge = Const.getPool().getBridge(endPoint);
                    OperationQueryFileRequest queryFileRequest = new OperationQueryFileRequest();
                    queryFileRequest.setMd5(pathOrMd5);
                    bridge.sendRequest(Const.O_QUERY_FILE, queryFileRequest, 0, null);
                    FileEntity fileEntity = (FileEntity) bridge.receiveResponse(tracker, QueryFileResponseHandler.class, null);
                    if (null != fileEntity) {
                        return fileEntity;
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

    /*@Override
    public Map<String, List<String>> upload(HttpServletRequest request, String group, IMonitor<MonitorProgressBean> monitor) throws Exception {

        Tuple tuple = selectUsableStorageMember(group, true);
        ExpireMember member = tuple.getF();
        Bridge connBridge = tuple.getS();

        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            throw new IllegalStateException("Invalid multipart form data.");
        }
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload();

        Map<String, List<String>> ret = new HashMap<>();
        // Parse the request
        FileItemIterator iter = upload.getItemIterator(request);
        while (iter.hasNext()) {
            FileItemStream item = iter.next();
            String name = item.getFieldName();
            InputStream stream = item.openStream();
            if (item.isFormField()) {
                String value = Streams.asString(stream);
                log.debug("Form field {} with value {} detected.", name, value);
                List<String> vals = ret.get(name);
                if (null == vals) {
                    vals = new ArrayList<>(3);
                    ret.put(name, vals);
                }
                vals.add(value);
            } else {
                log.debug("File field {} with file name {} detected.", name, item.getName());
                // Process the input stream
                if (!member.isHttpEnable()) {
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
            }
        }
    }*/

    @Override
    public String upload(HttpServletRequest request, String group, IMonitor<MonitorProgressBean> monitor, String protocol) throws Exception {
        protocol = null == protocol ? "http" : protocol.toLowerCase();
        if (!"http".equals(protocol) && !"https".equals(protocol)) {
            protocol = "http";
        }

        Set<ExpireMember> members = MemberManager.getMembersByGroup(group, false);
        if (null == members || members.isEmpty()) {
            throw new IllegalStateException("No storage server available[1].");
        }
        boolean dutyStream = false;
        for (ExpireMember mem : members) {
            if (!mem.isHttpEnable()) {
                continue;
            }
            OutputStream ops = null;
            HttpURLConnection connection = null;
            InputStream rips = null;
            try {
                URL url = new URL(protocol + "://" + mem.getAddr() + ":" + mem.getHttpPort() +"/upload");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    connection.addRequestProperty(name, request.getHeader(name));
                }
                connection.connect();
                ops = connection.getOutputStream();
                InputStream ips = request.getInputStream();
                byte[] buffer = new byte[10204];
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
            } catch (IOException e) {
                //e.printStackTrace();
                log.info("connection error with storage server {}:{} duo to: {}", mem.getAddr(), mem.getHttpPort(), e.getMessage());
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

    //TODO
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
    private ExpireMember selectStorageMember(Set<ExpireMember> members, Set<ExpireMember> exclude, String instanceId, boolean forUpload) {
        ExpireMember theOne = null;
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
        return theOne;
    }



    private Tuple selectUsableStorageMember(String group, boolean forUpload) {
        Set<ExpireMember> excludes = null;
        ExpireMember member;
        Bridge connBridge;
        Set<ExpireMember> members = MemberManager.getMembersByGroup(forUpload ? group : null, false);
        if (null == members || members.isEmpty()) {
            throw new IllegalStateException("No storage server available[1].");
        }
        while(true) {
            member = selectStorageMember(members, excludes, null, forUpload);
            if (null == member) {
                throw new IllegalStateException("No storage server available[4].");
            }
            try {
                log.debug("try to get connection for storage server[{}:{}].", member.getAddr(), member.getPort());
                connBridge = Const.getPool().getBridge(member.getEndPoint());
                return new Tuple(member, connBridge);
            } catch (Exception e) {
                if (null == excludes) {
                    excludes = new HashSet<>(2);
                    excludes.add(member);
                }
                log.error("error getting connection for storage server[{}:{}] duo to: {}", member.getAddr(), member.getPort(), e.getMessage());
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
