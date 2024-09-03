package cn.eroad.rad.listener;

import cn.eroad.rad.netty.UdpNettyStart;
import cn.eroad.device.event.CacheLoadedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @project: ecai-any-udp-rad
 * @ClassName: CacheLoadingCompletedListener
 * @author: liyongqiang
 * @creat: 2022/10/11 11:08
 * 描述:
 */
@Slf4j
@Component
public class CacheLoadingCompletedListener implements ApplicationListener<CacheLoadedEvent> {

    @Autowired
    private UdpNettyStart udpNettyStart;

    @Override
    public void onApplicationEvent(CacheLoadedEvent event) {
        log.info("设备缓存加载完成，启动udp服务");
        udpNettyStart.init();
    }

}
