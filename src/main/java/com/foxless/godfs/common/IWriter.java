package com.foxless.godfs.common;

import java.io.OutputStream;

public interface IWriter {
    void write(OutputStream ops) throws Exception;
}
