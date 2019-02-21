package com.foxless.godfs.common;

/**
 * Monitor for progress listening.
 * @param <T>
 *
 * @author hehety
 * @sine 1.0
 * @date 2018/09/27
 * @version 1.0
 */
public interface IMonitor<T> {
    void monitor(T o);
}
