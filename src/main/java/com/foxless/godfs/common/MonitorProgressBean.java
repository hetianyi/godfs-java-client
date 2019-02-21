package com.foxless.godfs.common;

public class MonitorProgressBean {
    /**
     * total bytes for upload
     */
    private long total;
    /**
     * finish upload bytes
     */
    private long finish;

    public MonitorProgressBean() {
    }

    public MonitorProgressBean(long total, long finish) {
        this.total = total;
        this.finish = finish;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getFinish() {
        return finish;
    }

    public void setFinish(long finish) {
        this.finish = finish;
    }
}
