package com.foxless.godfs.util;

import com.foxless.godfs.bean.ExpireMember;
import com.foxless.godfs.bean.Meta;
import com.foxless.godfs.common.Const;

public class Utils {

    public static String getStorageServerUID(ExpireMember expireMember) {
        return expireMember.getAddr() + ":" + expireMember.getPort() + ":" + expireMember.getGroup() + ":" + expireMember.getInstance_id();
    }

}

    // create a new tcp request using given data
// operation: operation code, such as 'O_CONNECT'
// meta     : meta object
// bodyLen  : request body length
// if create success, it returns a *Request, or else returns with error
public static Meta createMeta(int operation, Meta meta, long bodyLen) throws Exception {
    // operation bytes not found
    if (!Const.containsOperationCode(operation)) {
        throw new UnsupportedOperationException("operation not support");
    }

    metaBodyBytes, e := json.Marshal()
    if e != nil {
    return nil, e
    }

    metaLen := uint64(len(metaBodyBytes))

    return &Meta{
    operation,
    metaLen,
    bodyLen,
    metaBodyBytes,
    nil,
    }, nil
    }
