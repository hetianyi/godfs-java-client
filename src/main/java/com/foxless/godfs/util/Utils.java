package com.foxless.godfs.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.foxless.godfs.bean.ExpireMember;
import com.foxless.godfs.bean.Meta;
import com.foxless.godfs.common.Const;

public class Utils {

    private static ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static String getStorageServerUID(ExpireMember expireMember) {
        return expireMember.getAddr() + ":" + expireMember.getPort() + ":" + expireMember.getGroup() + ":" + expireMember.getInstance_id();
    }



    // create a new tcp request using given data
    // operation: operation code, such as 'O_CONNECT'
    // meta     : meta object
    // bodyLen  : request body length
    // if create success, it returns a *Request, or else returns with error
    public static Meta createMeta(int operation, Object operationMeta, long bodyLen) throws JsonProcessingException {
        Meta meta = new Meta();
        // operation bytes not found
        if (!Const.containsOperationCode(operation)) {
            meta.setError(new UnsupportedOperationException("operation not support"));
            return meta;
        }
        // String metaStr = JSON.toJSONString(operationMeta);
        String metaStr = objectMapper.writeValueAsString(operationMeta);
        meta.setOperation(operation);
        meta.setMetaBody(metaStr.getBytes());
        meta.setMetaLength(meta.getMetaBody().length);
        meta.setError(null);
        meta.setBodyLength(bodyLen);
        return meta;
    }




}


