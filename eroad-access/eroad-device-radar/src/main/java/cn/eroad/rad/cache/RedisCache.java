package cn.eroad.rad.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mrChen
 * @date 2022/7/7 16:57
 */
public class RedisCache {
    public static Map<String, String> snToRealsn = new ConcurrentHashMap<>();
}
