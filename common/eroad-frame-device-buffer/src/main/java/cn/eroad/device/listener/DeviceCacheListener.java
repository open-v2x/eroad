package cn.eroad.device.listener;

import cn.eroad.device.constants.DeviceConstant;
import cn.eroad.device.operate.DeviceCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyspaceEventMessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

/**
 * @project: eroad-frame
 * @ClassName: DeviceCacheListener
 * @author: liyongqiang
 * @creat: 2022/5/17 10:09
 * 描述: 监听redis设备缓存刷新的事件
 */

@Slf4j
public class DeviceCacheListener extends KeyspaceEventMessageListener {

    private static final Topic KEYEVENT_EXPIRED_TOPIC = new PatternTopic("__keyspace@*__:" + DeviceConstant.DEVICE_CACHE_CHANGE_KEY + "*");


    public DeviceCacheListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    public DeviceCacheListener(RedisMessageListenerContainer listenerContainer, DeviceCache deviceCache) {
        super(listenerContainer);
        this.deviceCache = deviceCache;
    }

    protected void doRegister(RedisMessageListenerContainer listenerContainer) {
        listenerContainer.addMessageListener(this, KEYEVENT_EXPIRED_TOPIC);
    }

    //    @Autowired
    private DeviceCache deviceCache;

    protected void doHandleMessage(Message message) {
        if (!new String(message.getBody()).equals("set")) {
            return;
        }


        String key = new String(message.getChannel()).split("__:")[1];
        log.info("监听到redis 键值变化，key：{}", key);
        deviceCache.updateLocalCache(key);

    }






}

