package cn.eroad.device.config;

import cn.eroad.device.operate.DeviceCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;




/*
 * @project: eroad-frame
 * @ClassName: DeviceCacheConfig
 * @author: liyongqiang
 * @creat: 2022/5/30 8:39
 */

@ConditionalOnProperty(name = "eroad.device.cache.listener", havingValue = "open")
@Configuration
@Slf4j
public class DeviceCacheConfig {

    @Autowired
    private cn.eroad.device.operate.DeviceCache deviceCache;





















































































    @Bean
    public Object deviceCacheRunner() throws Exception {

        boolean r = deviceCache.initLocalMap();
        log.info("从中心节点获取设备信息初始化本地缓存Map，结果：{}", r);
        return null;
    }

}
