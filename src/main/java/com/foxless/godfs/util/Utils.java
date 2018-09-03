package com.foxless.godfs.util;

import com.alibaba.fastjson.JSON;
import com.foxless.godfs.bean.ExpireMember;
import com.foxless.godfs.bean.Meta;
import com.foxless.godfs.common.Const;

import java.io.InputStream;

public class Utils {

    public static String getStorageServerUID(ExpireMember expireMember) {
        return expireMember.getAddr() + ":" + expireMember.getPort() + ":" + expireMember.getGroup() + ":" + expireMember.getInstance_id();
    }



    // create a new tcp request using given data
    // operation: operation code, such as 'O_CONNECT'
    // meta     : meta object
    // bodyLen  : request body length
    // if create success, it returns a *Request, or else returns with error
    public static Meta createMeta(int operation, Object operationMeta, long bodyLen) {
        Meta meta = new Meta();
        // operation bytes not found
        if (!Const.containsOperationCode(operation)) {
            meta.setError(new UnsupportedOperationException("operation not support"));
            return meta;
        }
        String metaStr = JSON.toJSONString(operationMeta);
        meta.setOperation(operation);
        meta.setMetaBody(metaStr.getBytes());
        meta.setMetaLength(meta.getMetaBody().length);
        meta.setError(null);
        meta.setBodyLength(bodyLen);
        return meta;
    }




}


