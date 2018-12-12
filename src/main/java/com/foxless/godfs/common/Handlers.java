package com.foxless.godfs.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class store  all kinds of ResponseHandler
 *
 * @author hehety
 * @sine 1.0
 * @date 2018/09/26
 * @version 1.0
 */
public class Handlers {

    private static final Logger log = LoggerFactory.getLogger(Handlers.class);

    private static final Map<Class, Object> cachedHandlers = new HashMap<Class, Object>(5);

    public static <T> T getHandler(Class<T> type) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        Object o = cachedHandlers.get(type);
        if (o == null) {
            synchronized (cachedHandlers) {
                if (null == cachedHandlers.get(type)) {
                    log.info("init handler for class: {}", type.getName());
                    o = Class.forName(type.getName(), true, Handlers.class.getClassLoader()).newInstance();
                    if (o instanceof IResponseHandler) {
                        log.info("init handler success for class: {}", type.getName());
                        cachedHandlers.put(type, o);
                    } else {
                        throw new IllegalArgumentException("class type must be " + IResponseHandler.class.getName() + " or it's subclass");
                    }
                } else { // fix bug: java.lang.NullPointerException
                    o = cachedHandlers.get(type);
                }
            }
        }
        return (T) o;
    }
}
