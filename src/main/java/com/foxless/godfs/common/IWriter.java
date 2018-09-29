package com.foxless.godfs.common;

import java.io.OutputStream;

public interface IWriter {
    /**
     * DO NOT CLOSE the OutputStream!
     * @param ops
     * @throws Exception
     */
    void write(OutputStream ops, long length) throws Exception;
}
