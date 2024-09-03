package cn.eroad.core.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class ReqIdUtil {
    private static volatile AtomicInteger reqId = new AtomicInteger(1);

    public static int nextReqId() {
        int nextId = reqId.addAndGet(1);
        if (nextId < 0) {
            reqId.set(2);
            nextId = 2;
        }
        return nextId;
    }
}
