package com.foxless.godfs.common;

import com.foxless.godfs.bean.MonitorProgressBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadProgressMonitor implements IMonitor<MonitorProgressBean> {

    private static final Logger log = LoggerFactory.getLogger(UploadProgressMonitor.class);

    @Override
    public void monitor(MonitorProgressBean o) {
        System.out.print("upload status:["+ o.getFinish() +"/"+ o.getTotal() +"]\r");
    }
}
