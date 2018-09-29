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
    public void write(OutputStream ops, long length) throws Exception {
        byte[] buffer = new byte[Const.BUFFER_SIZE];
        int len;
        long finish = 0l;
        long left = length;
        int nextRead = Const.BUFFER_SIZE;
        if (length < Const.BUFFER_SIZE) {
            nextRead = (int) left;
            while(left > 0 && (len = ips.read(buffer, 0, nextRead)) != -1) {
                ops.write(buffer, 0, len);
                finish += len;
                if (null != monitor) {
                    progressBean.setFinish(finish);
                    monitor.monitor(progressBean);
                }
                left = length - finish;
                nextRead = (int) left;
            }
        } else {
            while(left > 0 && (len = ips.read(buffer, 0, nextRead)) != -1) {
                ops.write(buffer, 0, len);
                finish += len;
                if (null != monitor) {
                    progressBean.setFinish(finish);
                    monitor.monitor(progressBean);
                }
                left = length - finish;
                if (left < Const.BUFFER_SIZE) {
                    nextRead = (int) left;
                }
            }
        }
    }
}
