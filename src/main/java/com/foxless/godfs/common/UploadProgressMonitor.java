package com.foxless.godfs.common;

public class UploadProgressMonitor implements IMonitor<MonitorProgressBean> {

    @Override
    public void monitor(MonitorProgressBean o) {
        System.out.print("upload status:["+ o.getFinish() +"/"+ o.getTotal() +"]\r");
    }
}
