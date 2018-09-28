package com.foxless.godfs.common;

import com.foxless.godfs.bean.MonitorProgressBean;

import java.io.InputStream;
import java.io.OutputStream;

public class UploadStreamWriter implements IWriter {

    private InputStream ips;
    private IMonitor monitor;
    private final MonitorProgressBean progressBean;
    public UploadStreamWriter(InputStream ips, IMonitor monitor) {
        this.ips = ips;
        this.monitor = monitor;
        this.progressBean = new MonitorProgressBean(0, 0);
    }

    @Override
    public void write(OutputStream ops) throws Exception {
        byte[] buffer = new byte[10240];
        int len;
        long finish = 0l;
        while((len = ips.read(buffer)) != -1) {
            ops.write(buffer, 0, len);
            if (null != monitor) {
                finish += len;
                progressBean.setFinish(finish);
                monitor.monitor(progressBean);
            }
        }
    }
}
