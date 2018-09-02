package com.foxless.godfs.common;

import com.foxless.godfs.bean.Meta;
import lombok.experimental.var;

import java.io.IOException;
import java.net.Socket;

public class Bridge {
    private Socket connection;
    private String uuid;

    public Bridge() {
    }

    public Bridge(Socket connection) {
        this.connection = connection;
    }

    public void close() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Socket GetConn() {
        return this.connection;
    }


    public synchronized sendRequest(int operation, Meta meta, long bodyLen, IWriter bodyWriterHandler) {
        request, e2 := CreateMeta(operation, meta, bodyLen)
        if e2 != nil {
            return e2
        }
        metaLenBytes := convertLen2Bytes(request.metaLength)
        bodyLenBytes := convertLen2Bytes(request.BodyLength)

        var headerBuff bytes.Buffer
        headerBuff.Write(operationHeadMap[request.Operation])
        headerBuff.Write(metaLenBytes)
        headerBuff.Write(bodyLenBytes)
        headerBuff.Write(request.MetaBody)
        len1, e1 := bridge.connection.Write(headerBuff.Bytes())
        if e1 != nil {
            Close(bridge.connection)
            return e1
        }
        if len1 != headerBuff.Len() {
            Close(bridge.connection)
            return SEND_HEAD_BYTES_ERROR
        }
        app.UpdateIOOUT(int64(headerBuff.Len()))
        if request.BodyLength > 0 {
            // write request body bytes using custom writer handler.
            err := bodyWriterHandler(bridge.connection)
            if err != nil {
                Close(bridge.connection)
                return err
            }
        }
        return nil
    }



    public Socket getConnection() {
        return connection;
    }

    public void setConnection(Socket connection) {
        this.connection = connection;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
